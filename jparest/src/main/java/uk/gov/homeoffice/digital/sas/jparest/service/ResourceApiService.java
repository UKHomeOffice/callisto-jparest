package uk.gov.homeoffice.digital.sas.jparest.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.JpaRestRepository;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.ENTITY_TENANT_ID_FIELD_NAME;

@Service
public class ResourceApiService <T extends BaseEntity> {


    private final EntityUtils<T> entityUtils;
    private final JpaRestRepository<T, Serializable> repository;
    private final PlatformTransactionManager transactionManager;


    @Autowired
    public ResourceApiService(EntityUtils<T> entityUtils,
                              JpaRestRepository<T, Serializable> repository,
                              PlatformTransactionManager transactionManager) {

        this.entityUtils = entityUtils;
        this.repository = repository;
        this.transactionManager = transactionManager;
    }


    public List<T> getAllResources(UUID tenantId, SpelExpression filter, Pageable pageable) {
        return repository.findAllByTenantId(tenantId, filter, pageable);
    }

    public T getResource(Serializable id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public T getResource(Serializable id, String include, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId, include)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public T createResource(T payload, UUID tenantId) {

        payload.setTenantId(tenantId);

        var transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
        T result;
        try {
            result = repository.saveAndFlush(payload);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
        return result;
    }


    public void deleteResource(Serializable id, UUID tenantId) {

        var transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            repository.deleteByIdAndTenantId(id, tenantId);
            transactionManager.commit(transactionStatus);
        } catch (EmptyResultDataAccessException ex) {
            transactionManager.rollback(transactionStatus);
            throw new ResourceNotFoundException(id);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
    }


    public void updateResource(T existingResource, T payload) {

        var transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
        BeanUtils.copyProperties(payload, existingResource, this.entityUtils.getIdFieldName(), ENTITY_TENANT_ID_FIELD_NAME);

        try {
            repository.saveAndFlush(existingResource);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
    }


    public List<?> getRelatedResources(Serializable id,
                                       String relation,
                                       UUID tenantId,
                                       SpelExpression filter, Pageable pageable) {

        return repository.findAllByIdAndRelationAndTenantId(
                id, relation, this.entityUtils.getRelatedType(relation), tenantId, filter, pageable);
    }


    public void deleteRelatedResources(T originalEntity,
                                       String relation,
                                       List<Serializable> relatedIds,
                                       UUID tenantId) {

        validateRelatedResourcesTenantIds(relation, relatedIds, tenantId);

        var transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        var relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
        var relatedEntityIdToEntityMap = relatedEntities.stream()
                .map(BaseEntity.class::cast)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        var notDeletableRelatedIds = new HashSet<>();
        for (var relatedId: relatedIds) {
            var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, relatedId);
            if (!relatedEntities.remove(relatedEntityIdToEntityMap.get(entityReference.getId()))) {
                notDeletableRelatedIds.add(relatedId);
            }
        }

        if (!notDeletableRelatedIds.isEmpty()) {
            throw new ResourceNotFoundException(deletableRelatedResourcesMessage(
                    entityUtils.getRelatedType(relation), notDeletableRelatedIds));
        }

        try {
            repository.saveAndFlush(originalEntity);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

    }

    public void addRelatedResources(T originalEntity,
                                    String relation,
                                    List<Serializable> relatedIds,
                                    UUID tenantId) {

        validateRelatedResourcesTenantIds(relation, relatedIds, tenantId);

        var transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        var relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
        var relatedEntityIdToEntityMap = relatedEntities.stream()
                .map(entity -> (BaseEntity) entity)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        for (var relatedId: relatedIds) {
            var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, relatedId);
            if (!relatedEntityIdToEntityMap.containsKey(entityReference.getId()))  relatedEntities.add(entityReference);
        }

        try {
            repository.saveAndFlush(originalEntity);
            transactionManager.commit(transactionStatus);
        } catch (EntityNotFoundException ex){
            transactionManager.rollback(transactionStatus);
            throw new ResourceNotFoundException(originalEntity.getId());
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
    }


    private void validateRelatedResourcesTenantIds(String relation, Collection<Serializable> relatedIds, UUID tenantId) {

        var relatedResourcesCount = repository.countAllByRelationAndTenantId(
                this.entityUtils.getRelatedType(relation), relatedIds, tenantId);

        if (relatedResourcesCount != relatedIds.size())
            throw new ResourceNotFoundException(relatedResourcesMessage(relatedIds));
    }



}
