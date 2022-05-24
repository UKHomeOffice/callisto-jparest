package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.utils.ValidatorUtils;
import uk.gov.homeoffice.digital.sas.jparest.utils.WebDataBinderFactory;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;

/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
@ResponseBody
public class ResourceApiController<T extends BaseEntity, U> {

    @Getter
    private Class<T> entityType;
    private EntityManager entityManager;
    private PersistenceUnitUtil persistenceUnitUtil;
    private PlatformTransactionManager transactionManager;
    private JpaRepository<T, Serializable> repository;
    private EntityUtils<T> entityUtils;
    private final ValidatorUtils validatorUtils = new ValidatorUtils();

    private static WebDataBinder binder = WebDataBinderFactory.getWebDataBinder();
    private static final String ENTITY_TENANT_ID_FIELD_NAME = "tenantId";
    private static final String ENTITY_ID_FIELD_NAME = "id";
    private static final String QUERY_HINT = "javax.persistence.fetchgraph";


    private @NonNull Serializable getIdentifier(Object identifier) {
        return getIdentifier(identifier, this.entityUtils.getIdFieldType());
    }

    private static @NonNull Serializable getIdentifier(Object identifier, Class<?> fieldType) {
        Serializable result = (Serializable) binder.convertIfNecessary(identifier, fieldType);
        if (result == null) {
            throw new IllegalArgumentException("identifier must not be null");
        }
        return result;
    }



    @SuppressWarnings("unchecked")
    public ResourceApiController(Class<T> entityType, EntityManager entityManager,
                                 PlatformTransactionManager transactionManager, EntityUtils<?> entityUtils) {
        this.entityType = entityType;
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
        this.repository = new SimpleJpaRepository<>(entityType, entityManager);
        this.entityUtils = (EntityUtils<T>) entityUtils;
        this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    public ApiResponse<T> list(SpelExpression filter, Pageable pageable, @RequestParam UUID tenantId) {

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        var tenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
        var finalPredicate = filter != null
                ? builder.and(tenantPredicate, filterPredicate) : tenantPredicate;
        query.where(finalPredicate);

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());

        CriteriaQuery<T> select = query.select(root);
        List<Order> orderBy = toOrders(pageable.getSort(), root, builder);
        select.orderBy(orderBy);

        TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        typedQuery.setHint(QUERY_HINT, entityGraph);
        List<T> result = typedQuery.getResultList();
        return new ApiResponse<>(result);
    }

    private T getById(U id, String include, UUID tenantId) {

        Serializable identifier = getIdentifier(id);

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());


        var tenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var filterPredicate = builder.equal(root.get(ENTITY_ID_FIELD_NAME), identifier);
        var finalPredicate = builder.and(tenantPredicate, filterPredicate);
        query.where(finalPredicate);

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
        if (StringUtils.hasText(include)) {
            entityGraph.addAttributeNodes(include);
        }

        CriteriaQuery<T> select = query.select(root);
        TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setHint(QUERY_HINT, entityGraph);
        List<T> result2 = typedQuery.getResultList();

        if (result2.isEmpty()) throw new ResourceNotFoundException(id);
        return result2.get(0);
    }

    public ApiResponse<T> get(@PathVariable U id, @RequestParam UUID tenantId) {
        var result = getById(id, null, tenantId);
        return new ApiResponse<>(Arrays.asList(result));
    }



    public ApiResponse<T> create(@RequestBody String body, @RequestParam UUID tenantId) throws JsonProcessingException {

        T r2 = readPayload(body);
        validateTenantIdPayloadMatch(tenantId, r2);

        this.validatorUtils.validateAndThrowIfErrorsExist(r2);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
        T result;
        try {
            result = repository.saveAndFlush(r2);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
        return new ApiResponse<>(Arrays.asList(result));
    }

    public void delete(@PathVariable U id, @RequestParam UUID tenantId) {

        var identifier = getIdentifier(id);

        T resource = repository.findById(identifier).orElseThrow(() -> new ResourceNotFoundException(id));
        validateResourceTenantId(tenantId, resource, id);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        try {
            repository.deleteById(identifier);
            transactionManager.commit(transactionStatus);
        } catch (EmptyResultDataAccessException ex) {
            transactionManager.rollback(transactionStatus);
            throw new ResourceNotFoundException(id);

        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
    }

    public ApiResponse<T> update(@PathVariable U id,
                                 @RequestBody String body,
                                 @RequestParam UUID tenantId) throws JsonProcessingException {

        var identifier = getIdentifier(id);
        T r2 = readPayload(body);
        validateTenantIdPayloadMatch(tenantId, r2);

        var payloadEntityId = this.persistenceUnitUtil.getIdentifier(r2);
        if (payloadEntityId != null && !identifier.equals(getIdentifier(payloadEntityId))) {
            throw new IllegalArgumentException("The supplied payload resource id value must match the url id path parameter value");
        }

        this.validatorUtils.validateAndThrowIfErrorsExist(r2);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        T orig = repository.findById(identifier).orElseThrow(() ->
                new ResourceNotFoundException(id));

        validateResourceTenantId(tenantId, orig, id);

        BeanUtils.copyProperties(r2, orig, this.entityUtils.getIdFieldName());

        try {
            repository.saveAndFlush(orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        return new ApiResponse<>(Arrays.asList(orig));
    }

    @SuppressWarnings("rawtypes")
    public ApiResponse getRelated(@PathVariable U id,
                                  @PathVariable String relation,
                                  SpelExpression filter, Pageable pageable,
                                  @RequestParam UUID tenantId) {

        Serializable identifier = getIdentifier(id);
        var builder = this.entityManager.getCriteriaBuilder();
        Class<?> relatedEntityType = this.entityUtils.getRelatedType(relation);
        CriteriaQuery<?> query = builder.createQuery(relatedEntityType);
        Root<T> root = query.from(this.entityUtils.getEntityType());
        CriteriaQuery<?> select = query.select(root.join(relation));
        Join<?, ?> relatedJoin = root.getJoins().iterator().next();

        var predicate = builder.equal(root.get(ENTITY_ID_FIELD_NAME), identifier);
        var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, relatedJoin);
        if (filterPredicate != null) {
            predicate = builder.and(predicate, filterPredicate);
        }
        var parentTenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var relatedTenantPredicate = builder.equal(relatedJoin.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var finalPredicate = builder.and(parentTenantPredicate, relatedTenantPredicate, predicate);
        select.where(finalPredicate);

        List<Order> orderBy = toOrders(pageable.getSort(), relatedJoin, builder);
        select.orderBy(orderBy);

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
        TypedQuery<?> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        typedQuery.setHint(QUERY_HINT, entityGraph);

        List<?> result = typedQuery.getResultList();
        return new ApiResponse<>(result);
    }



    public void deleteRelated(@PathVariable U id,
                              @PathVariable String relation,
                              @PathVariable Object[] relatedIds,
                              @RequestParam UUID tenantId) throws IllegalArgumentException {

        var originalEntity = getById(id, relation, tenantId);
        validateRelatedResourcesTenantIds(relation, relatedIds, tenantId);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        var relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
        var relatedEntityIdToEntityMap = relatedEntities.stream().map(entity -> (BaseEntity) entity)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);

        var notDeletableRelatedIds = new HashSet<>();
        for (var relatedId: relatedIds) {
            var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, getIdentifier(relatedId, relatedIdType));
            if (!relatedEntities.remove(relatedEntityIdToEntityMap.get(entityReference.getId()))) notDeletableRelatedIds.add(relatedId);
        }
        if (!notDeletableRelatedIds.isEmpty()) {
            Class<?> relatedType = entityUtils.getRelatedType(relation);
            throw new ResourceNotFoundException(deletableRelatedResourcesMessage(relatedType, notDeletableRelatedIds));
        }

        try {
            repository.saveAndFlush(originalEntity);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
    }


    public void addRelated(@PathVariable U id,
                           @PathVariable String relation,
                           @PathVariable Object[] relatedIds,
                           @RequestParam UUID tenantId) throws IllegalArgumentException {


        var originalEntity = getById(id, relation, tenantId);
        validateRelatedResourcesTenantIds(relation, relatedIds, tenantId);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        Collection<Object> relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
        var relatedEntityIdToEntityMap = relatedEntities.stream().map(entity -> (BaseEntity) entity)
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);

        for (var relatedId: relatedIds) {
            var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, getIdentifier(relatedId, relatedIdType));
            if (!relatedEntityIdToEntityMap.containsKey(entityReference.getId()))  relatedEntities.add(entityReference);
        }

        try {
            repository.saveAndFlush(originalEntity);
            transactionManager.commit(transactionStatus);
        } catch (EntityNotFoundException ex){
            transactionManager.rollback(transactionStatus);
            throw new ResourceNotFoundException(id);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

    }

    private static List<Order> toOrders(Sort sort, Path<?> path, CriteriaBuilder builder) {

        if (sort.isUnsorted()) {
            return Collections.emptyList();
        }

        Assert.notNull(path, "Path must not be null!");
        Assert.notNull(builder, "CriteriaBuilder must not be null!");

        List<javax.persistence.criteria.Order> orders = new ArrayList<>();

        for (org.springframework.data.domain.Sort.Order sortOrder : sort) {
            Order order;
            if (sortOrder.isAscending()) {
                order = builder.asc(path.get(sortOrder.getProperty()));
            } else {
                order = builder.desc(path.get(sortOrder.getProperty()));
            }
            orders.add(order);
        }

        return orders;

    }


    private T readPayload(String body) throws JsonProcessingException {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, this.entityUtils.getEntityType());
        } catch (UnrecognizedPropertyException ex) {
            throw new UnknownResourcePropertyException(ex.getPropertyName(), ex.getReferringClass().getSimpleName());
        }
    }

    private void validateResourceTenantId(UUID requestTenantId, T resource, U resourceId) {
        if (!requestTenantId.equals(resource.getTenantId()))
            throw new ResourceNotFoundException(resourceId);
    }

    private void validateRelatedResourcesTenantIds(String relation, Object[] relatedIds, UUID tenantId) {

        var builder = this.entityManager.getCriteriaBuilder();
        var query = builder.createQuery(Long.class);
        var relatedRoot = query.from(this.entityUtils.getRelatedType(relation));
        var serializedRelatedIds = Arrays.stream(relatedIds)
                .map(relatedId -> getIdentifier(relatedId, entityUtils.getRelatedIdType(relation))).toList();
        var relatedIdPredicate = relatedRoot.get(this.entityUtils.getRelatedIdField(relation).getName()).in(serializedRelatedIds);
        var relatedTenantPredicate = builder.equal(relatedRoot.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var relatedSelect = query.select(builder.count(relatedRoot)).where(builder.and(relatedIdPredicate, relatedTenantPredicate));
        var tenantIdMatchesRelatedResources = this.entityManager.createQuery(relatedSelect).getSingleResult() == relatedIds.length;

        if (!tenantIdMatchesRelatedResources) throw new ResourceNotFoundException(
                    relatedResourcesMessage(Arrays.stream(relatedIds).collect(Collectors.toSet())));
    }

    private void validateTenantIdPayloadMatch(UUID requestTenantId, T payload) {

        if (payload.getTenantId() != null && !requestTenantId.equals(payload.getTenantId())) {
            throw new TenantIdMismatchException("The supplied payload tenant id value must match the url tenant id query parameter value");

        } else if (payload.getTenantId() == null) {
            payload.setTenantId(requestTenantId);
        }
    }



}
