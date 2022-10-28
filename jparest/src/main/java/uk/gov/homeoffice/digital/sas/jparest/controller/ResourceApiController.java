package uk.gov.homeoffice.digital.sas.jparest.controller;

import static uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter.ID;
import static uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter.TENANT_ID;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnexpectedQueryResultException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 *
 * <p>Resources for ManyToMany relationships can also be queried.
 */
@ResponseBody
@Slf4j
public class ResourceApiController<T extends BaseEntity> {

  @Getter
  private final Class<T> entityType;
  private final EntityManager entityManager;
  private final PersistenceUnitUtil persistenceUnitUtil;
  private final PlatformTransactionManager transactionManager;
  private final JpaRepository<T, Serializable> repository;
  private final EntityUtils<T, ?> entityUtils;
  private final EntityValidator entityValidator;
  private final ObjectMapper objectMapper;

  private static final String QUERY_HINT = "javax.persistence.fetchgraph";


  @SuppressWarnings("unchecked")
  public ResourceApiController(Class<T> entityType, EntityManager entityManager,
                               PlatformTransactionManager transactionManager,
                               EntityUtils<?, ?> entityUtils,
                               EntityValidator entityValidator,
                               ObjectMapper objectMapper) {
    this.entityType = entityType;
    this.entityManager = entityManager;
    this.transactionManager = transactionManager;
    this.repository = new SimpleJpaRepository<>(entityType, entityManager);
    this.entityUtils = (EntityUtils<T, ?>) entityUtils;
    this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    this.entityValidator = entityValidator;
    this.objectMapper = objectMapper;
  }


  public ApiResponse<T> list(
      @RequestParam UUID tenantId, Pageable pageable, SpelExpression filter) {

    var builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
    Root<T> root = query.from(this.entityUtils.getEntityType());

    var tenantPredicate = builder.equal(root.get(TENANT_ID.getParamName()), tenantId);
    var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
    var finalPredicate =
        filter != null ? builder.and(tenantPredicate, filterPredicate) : tenantPredicate;
    query.where(finalPredicate);

    CriteriaQuery<T> select = query.select(root);
    List<Order> orderBy = toOrders(pageable.getSort(), root, builder);
    select.orderBy(orderBy);

    EntityGraph<T> entityGraph =
        this.entityManager.createEntityGraph(this.entityUtils.getEntityType());

    TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());
    typedQuery.setHint(QUERY_HINT, entityGraph);
    List<T> result = typedQuery.getResultList();
    return new ApiResponse<>(result);
  }

  private T getById(UUID id, String include, UUID tenantId) {

    var builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
    Root<T> root = query.from(this.entityUtils.getEntityType());


    var tenantPredicate = builder.equal(root.get(TENANT_ID.getParamName()), tenantId);
    var filterPredicate = builder.equal(root.get(ID.getParamName()), id);
    var finalPredicate = builder.and(tenantPredicate, filterPredicate);
    query.where(finalPredicate);

    EntityGraph<T> entityGraph =
          this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
    if (StringUtils.hasText(include)) {
      entityGraph.addAttributeNodes(include);
    }

    CriteriaQuery<T> select = query.select(root);
    TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
    typedQuery.setHint(QUERY_HINT, entityGraph);
    List<T> result2 = typedQuery.getResultList();

    if (result2.isEmpty()) {
      throw new ResourceNotFoundException(id);
    }
    return result2.get(0);
  }

  public ApiResponse<T> get(@RequestParam UUID tenantId, @PathVariable UUID id) {
    var result = getById(id, null, tenantId);
    return new ApiResponse<>(List.of(result));
  }

  public ApiResponse<T> create(@RequestParam UUID tenantId, @RequestBody String body)
      throws JsonProcessingException {

    T r2 = readPayload(body);
    validateAndSetTenantIdPayloadMatch(tenantId, r2);

    this.entityValidator.validateAndThrowIfErrorsExist(r2);

    if (Objects.nonNull(r2.getId())) {
      throw new IllegalArgumentException(
        "A resource id should not be provided when creating a new resource.");
    }

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);
    T result;
    try {
      result = repository.saveAndFlush(r2);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
    return new ApiResponse<>(List.of(result));
  }

  public void delete(@RequestParam UUID tenantId, @PathVariable UUID id) {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    try {
      var builder = this.entityManager.getCriteriaBuilder();
      CriteriaDelete<T> query = builder.createCriteriaDelete(this.entityUtils.getEntityType());
      Root<T> root = query.from(this.entityUtils.getEntityType());

      var tenantPredicate = builder.equal(root.get(TENANT_ID.getParamName()), tenantId);
      var idPredicate = builder.equal(root.get(ID.getParamName()), id);
      query.where(builder.and(tenantPredicate, idPredicate));

      var updatedItems = this.entityManager.createQuery(query).executeUpdate();
      if (updatedItems == 0) {
        throw new EmptyResultDataAccessException(1);
      } else if (updatedItems > 1) {
        throw new UnexpectedQueryResultException(id);
      }

      transactionManager.commit(transactionStatus);
    } catch (EmptyResultDataAccessException ex) {
      transactionManager.rollback(transactionStatus);
      throw new ResourceNotFoundException(id);

    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
  }

  public ApiResponse<T> update(@RequestParam UUID tenantId, @PathVariable UUID id,
                               @RequestBody String body) throws JsonProcessingException {

    T payload = readPayload(body);
    validateAndSetTenantIdPayloadMatch(tenantId, payload);

    var payloadEntityId = (UUID) this.persistenceUnitUtil.getIdentifier(payload);
    if (payloadEntityId != null && !id.equals(payloadEntityId)) {
      throw new IllegalArgumentException(
        "The supplied payload resource id value must match the url id path parameter value");
    }

    payload.setId(id);
    this.entityValidator.validateAndThrowIfErrorsExist(payload);

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    var orig = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

    validateResourceTenantId(tenantId, orig, id);

    BeanUtils.copyProperties(payload, orig, EntityUtils.ID_FIELD_NAME);

    try {
      repository.saveAndFlush(orig);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }

    return new ApiResponse<>(List.of(orig));
  }

  @SuppressWarnings("squid:S1452") // Generic wildcard types should not be used in return parameters
  public ApiResponse<?> getRelated(
      @RequestParam UUID tenantId,
      @PathVariable UUID id,
      @PathVariable String relation, Pageable pageable, SpelExpression filter) {

    var builder = this.entityManager.getCriteriaBuilder();
    Class<?> relatedEntityType = this.entityUtils.getRelatedType(relation);
    CriteriaQuery<?> query = builder.createQuery(relatedEntityType);
    Root<T> root = query.from(this.entityUtils.getEntityType());
    CriteriaQuery<?> select = query.select(root.join(relation));
    Join<?, ?> relatedJoin = root.getJoins().iterator().next();

    var predicate = builder.equal(root.get(ID.getParamName()), id);
    var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, relatedJoin);
    if (filterPredicate != null) {
      predicate = builder.and(predicate, filterPredicate);
    }
    var parentTenantPredicate = builder.equal(root.get(TENANT_ID.getParamName()), tenantId);
    var relatedTenantPredicate = builder.equal(relatedJoin.get(TENANT_ID.getParamName()), tenantId);
    var finalPredicate = builder.and(parentTenantPredicate, relatedTenantPredicate, predicate);
    select.where(finalPredicate);

    List<Order> orderBy = toOrders(pageable.getSort(), relatedJoin, builder);
    select.orderBy(orderBy);

    EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(
        this.entityUtils.getEntityType());
    TypedQuery<?> typedQuery = this.entityManager.createQuery(select);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());
    typedQuery.setHint(QUERY_HINT, entityGraph);

    List<?> result = typedQuery.getResultList();
    return new ApiResponse<>(result);
  }

  public void deleteRelated(
      @RequestParam UUID tenantId,
      @PathVariable UUID id,
      @PathVariable String relation,
      @PathVariable List<UUID> relatedIds)
      throws IllegalArgumentException {

    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    var originalEntity = getById(id, relation, tenantId);

    var relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
    Map<UUID, BaseEntity> relatedEntityIdToEntityMap =
        relatedEntities.stream()
          .map(BaseEntity.class::cast)
          .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

    var notDeletableRelatedIds = new HashSet<UUID>();
    for (var relatedId : relatedIds) {
      var entityReference = (BaseEntity) this.entityUtils.getEntityReference(relation, relatedId);
      if (!relatedEntities.remove(relatedEntityIdToEntityMap.get(entityReference.getId()))) {
        notDeletableRelatedIds.add(relatedId);
      }
    }
    if (!notDeletableRelatedIds.isEmpty()) {
      Class<?> relatedType = entityUtils.getRelatedType(relation);
      throw new ResourceNotFoundException(
        deletableRelatedResourcesMessage(relatedType, notDeletableRelatedIds));
    }

    try {
      repository.saveAndFlush(originalEntity);
      transactionManager.commit(transactionStatus);
    } catch (RuntimeException ex) {
      transactionManager.rollback(transactionStatus);
      throw ex;
    }
  }

  public void addRelated(
        @RequestParam UUID tenantId,
        @PathVariable UUID id,
        @PathVariable String relation,
        @PathVariable List<UUID> relatedIds)
      throws IllegalArgumentException {


    var transactionDefinition = new DefaultTransactionDefinition();
    var transactionStatus =
        this.transactionManager.getTransaction(transactionDefinition);

    var originalEntity = getById(id, relation, tenantId);
    validateRelatedResourcesTenantIds(relation, relatedIds, tenantId);

    var relatedEntities = entityUtils.getRelatedEntities(originalEntity, relation);
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

    try {
      repository.saveAndFlush(originalEntity);
      transactionManager.commit(transactionStatus);
    } catch (EntityNotFoundException ex) {
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

    List<Order> orders = new ArrayList<>();

    for (Sort.Order sortOrder : sort) {
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
      return objectMapper.readValue(body, this.entityUtils.getEntityType());
    } catch (UnrecognizedPropertyException ex) {
      throw new UnknownResourcePropertyException(
        ex.getPropertyName(), ex.getReferringClass().getSimpleName());
    }
  }

  private void validateResourceTenantId(UUID requestTenantId, T resource, UUID resourceId) {
    if (!requestTenantId.equals(resource.getTenantId())) {
      throw new ResourceNotFoundException(resourceId);
    }
  }

  private void validateRelatedResourcesTenantIds(
      String relation, List<UUID> relatedIds, UUID tenantId) {
    var builder = this.entityManager.getCriteriaBuilder();
    var query = builder.createQuery(Long.class);
    var relatedRoot = query.from(this.entityUtils.getRelatedType(relation));
    var relatedIdPredicate = relatedRoot.get(EntityUtils.ID_FIELD_NAME).in(relatedIds);
    var relatedTenantPredicate =
        builder.equal(relatedRoot.get(TENANT_ID.getParamName()), tenantId);
    var relatedSelect =
        query.select(builder.count(relatedRoot))
        .where(builder.and(relatedIdPredicate, relatedTenantPredicate));
    var tenantIdMatchesRelatedResources =
        this.entityManager.createQuery(relatedSelect).getSingleResult() == relatedIds.size();
    if (!tenantIdMatchesRelatedResources) {
      throw new ResourceNotFoundException(relatedResourcesMessage(relatedIds));
    }
  }

  private void validateAndSetTenantIdPayloadMatch(UUID requestTenantId, T payload) {

    var payloadTenantId = payload.getTenantId();
    if (payloadTenantId != null && !requestTenantId.equals(payloadTenantId)) {
      throw new TenantIdMismatchException();

    } else if (payloadTenantId == null) {
      payload.setTenantId(requestTenantId);
    }
  }

  private void methodToTestSonar() {
    for (int i = 0; i < 5; i++) {
      log.info("for loop is running");
    }
  }


}
