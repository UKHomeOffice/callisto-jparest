package uk.gov.homeoffice.digital.sas.jparest.service;

import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;


@Service
public class ResourceApiService<T extends BaseEntity> {

  private final EntityUtils<T, ?> entityUtils;
  private final PlatformTransactionManager transactionManager;
  private final TenantRepository<T> repository;
  private final EntityValidator entityValidator;

  public ResourceApiService(EntityUtils<T, ?> entityUtils,
                            PlatformTransactionManager transactionManager,
                            TenantRepository<T> repository,
                            EntityValidator entityValidator) {
    this.entityUtils = entityUtils;
    this.transactionManager = transactionManager;
    this.repository = repository;
    this.entityValidator = entityValidator;
  }

  public List<T> getAllResources(UUID tenantId, Pageable pageable, SpelExpression filter) {
    return repository.findAllByTenantId(tenantId, filter, pageable);
  }

  public T getResource(UUID tenantId, UUID id) {
    return repository.findByTenantIdAndId(tenantId, id)
        .orElseThrow(() -> new ResourceNotFoundException(id));
  }

  public T createResource(T entity) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
    T result;
    try {
      this.entityValidator.validateAndThrowIfErrorsExist(entity);
      result = repository.saveAndFlush(entity);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
    return result;
  }

  public void deleteResource(UUID tenantId, UUID id) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    try {
      repository.deleteByTenantIdAndId(tenantId, id);
      transactionManager.commit(transactionStatus);
    } catch (NoSuchElementException ex) {
      transactionManager.rollback(transactionStatus);
      throw new ResourceNotFoundException(id);

    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
  }

  public T updateResource(T entity) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
    T originalEntity;
    try {
      this.entityValidator.validateAndThrowIfErrorsExist(entity);
      originalEntity = repository.findByTenantIdAndId(entity.getTenantId(), entity.getId())
              .orElseThrow(() -> new ResourceNotFoundException(entity.getId()));
      BeanUtils.copyProperties(entity, originalEntity, EntityUtils.ID_FIELD_NAME);
      repository.saveAndFlush(originalEntity);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }

    return originalEntity;
  }

  public void deleteRelatedResources(UUID tenantId,
                                     UUID id,
                                     String relation,
                                     List<UUID> relatedIds) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    try {
      T parentEntity = repository.findByTenantIdAndId(tenantId, id, relation)
          .orElseThrow(() -> new ResourceNotFoundException(id));
      Collection<?> relatedEntities = entityUtils.getRelatedEntities(parentEntity, relation);
      Map<UUID, BaseEntity> relatedEntityIdToEntityMap =
          relatedEntities.stream()
              .map(BaseEntity.class::cast)
              .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

      var notDeletableRelatedIds = new ArrayList<UUID>();
      for (var relatedId : relatedIds) {
        var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, relatedId);
        BaseEntity relatedEntityToDelete = relatedEntityIdToEntityMap.get(entityReference.getId());
        if (!relatedEntities.remove(relatedEntityToDelete)) {
          notDeletableRelatedIds.add(relatedId);
        }
      }
      if (!notDeletableRelatedIds.isEmpty()) {
        throw new ResourceNotFoundException(deletableRelatedResourcesMessage(
            entityUtils.getRelatedType(relation), notDeletableRelatedIds));
      }

      repository.saveAndFlush(parentEntity);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
  }

  public void addRelatedResources(UUID tenantId,
                                  UUID id,
                                  String relation,
                                  List<UUID> relatedIds) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    try {
      var parentEntity = repository.findByTenantIdAndId(tenantId, id, relation)
          .orElseThrow(() -> new ResourceNotFoundException(id));

      var totalMatchingRelations = repository.countAllByTenantIdAndRelation(
          tenantId, entityUtils.getRelatedType(relation), relatedIds);
      if (totalMatchingRelations != relatedIds.size()) {
        throw new ResourceNotFoundException(relatedResourcesMessage(relatedIds));
      }

      var relatedEntities = entityUtils.getRelatedEntities(parentEntity, relation);
      var relatedEntityIdToEntityMap =
          relatedEntities.stream()
              .map(BaseEntity.class::cast)
              .collect(Collectors
                  .toMap(BaseEntity::getId, Function.identity()));

      for (var relatedId : relatedIds) {
        var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, relatedId);
        if (!relatedEntityIdToEntityMap.containsKey(entityReference.getId())) {
          relatedEntities.add(entityReference);
        }
      }

      repository.saveAndFlush(parentEntity);
      transactionManager.commit(transactionStatus);
    } catch (EntityNotFoundException ex) {
      transactionManager.rollback(transactionStatus);
      throw new ResourceNotFoundException(id);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }

  }

  @SuppressWarnings("squid:S1452") // Generic wildcard types should not be used in return parameters
  public List<?> getRelatedResources(UUID tenantId,
                                     UUID id,
                                     String relation,
                                     Pageable pageable,
                                     SpelExpression filter) {
    return repository.findAllByTenantIdAndIdAndRelation(
        tenantId, id, relation, entityUtils.getRelatedType(relation), filter, pageable);
  }

  public UUID getEntityId(T entity) {
    return repository.findId(entity);
  }


}
