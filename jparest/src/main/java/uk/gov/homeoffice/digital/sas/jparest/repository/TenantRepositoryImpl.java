package uk.gov.homeoffice.digital.sas.jparest.repository;

import static uk.gov.homeoffice.digital.sas.jparest.utils.CommonUtils.getFieldNameOrThrow;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class TenantRepositoryImpl<T>
        extends SimpleJpaRepository<T, UUID> implements TenantRepository<T> {

  private final EntityManager entityManager;
  private final Class<T> entityType;
  private final String tenantIdFieldName;
  private final PersistenceUnitUtil persistenceUnitUtil;

  private static final String QUERY_HINT = "jakarta.persistence.fetchgraph";


  public TenantRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
    super(entityType, entityManager);
    this.entityManager = entityManager;
    this.entityType = entityType;
    this.tenantIdFieldName = getFieldNameOrThrow(BaseEntity.class, "tenantId");
    this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
  }


  @Override
  public List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(entityType);
    Root<T> root = query.from(entityType);

    Predicate tenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    Predicate filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
    Predicate finalPredicate =
        filter != null ? builder.and(tenantPredicate, filterPredicate) : tenantPredicate;
    query.where(finalPredicate);

    CriteriaQuery<T> select = query.select(root);
    select.orderBy(getOrderCriteria(pageable.getSort(), root, builder));

    return this.entityManager.createQuery(select)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .setHint(QUERY_HINT, entityManager.createEntityGraph(entityType))
        .getResultList();
  }


  @Override
  public Optional<T> findByTenantIdAndId(UUID tenantId, UUID id) {
    return this.findByTenantIdAndId(tenantId, id, null);
  }

  public Optional<T> findByTenantIdAndId(UUID tenantId, UUID id, String relatedResourceType) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(entityType);
    Root<T> root = query.from(entityType);

    Predicate tenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    Predicate idPredicate = builder.equal(root.get(EntityUtils.ID_FIELD_NAME), id);
    Predicate finalPredicate = builder.and(tenantPredicate, idPredicate);
    query.where(finalPredicate);

    EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityType);
    if (StringUtils.hasText(relatedResourceType)) {
      entityGraph.addAttributeNodes(relatedResourceType);
    }

    CriteriaQuery<T> select = query.select(root);
    return this.entityManager.createQuery(select)
        .setHint(QUERY_HINT, entityGraph)
        .getResultList()
        .stream().findFirst();
  }

  public List<T> findByTenantIdAndIds(UUID tenantId, Collection<UUID> ids) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(entityType);
    Root<T> root = query.from(entityType);

    Predicate tenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    Predicate idPredicate = root.get(EntityUtils.ID_FIELD_NAME).in(ids);
    Predicate finalPredicate = builder.and(tenantPredicate, idPredicate);
    query.where(finalPredicate);

    EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityType);

    CriteriaQuery<T> select = query.select(root);
    return this.entityManager.createQuery(select)
        .setHint(QUERY_HINT, entityGraph)
        .getResultList();
  }

  @Override
  public List<?> findAllByTenantIdAndIdAndRelation(UUID tenantId,
                                                   UUID id,
                                                   String relatedResourceType,
                                                   Class<?> relatedEntityClass,
                                                   SpelExpression filter,
                                                   Pageable pageable) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<?> query = builder.createQuery(relatedEntityClass);
    Root<T> root = query.from(entityType);
    CriteriaQuery<?> select = query.select(root.join(relatedResourceType));
    Join<?, ?> relatedJoin = root.getJoins().iterator().next();

    Predicate idPredicate = builder.equal(root.get(EntityUtils.ID_FIELD_NAME), id);
    Predicate filterPredicate = SpelExpressionToPredicateConverter.convert(
        filter, builder, relatedJoin);
    if (filterPredicate != null) {
      idPredicate = builder.and(idPredicate, filterPredicate);
    }
    Predicate parentTenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    Predicate relatedTenantPredicate = builder.equal(relatedJoin.get(tenantIdFieldName), tenantId);

    select.where(
        builder.and(parentTenantPredicate, relatedTenantPredicate, idPredicate));
    select.orderBy(getOrderCriteria(pageable.getSort(), relatedJoin, builder));

    return this.entityManager.createQuery(select)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .setHint(QUERY_HINT, entityManager.createEntityGraph(entityType))
        .getResultList();
  }

  @Override
  public Long countAllByTenantIdAndRelation(UUID tenantId,
                                            Class<?> relatedEntityClass,
                                            Collection<UUID> relatedIds) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = builder.createQuery(Long.class);
    Root<?> relatedRoot = query.from(relatedEntityClass);

    Predicate relatedIdPredicate = relatedRoot.get(EntityUtils.ID_FIELD_NAME).in(relatedIds);
    Predicate relatedTenantPredicate =
        builder.equal(relatedRoot.get(tenantIdFieldName), tenantId);

    CriteriaQuery<Long> relatedSelect =
        query.select(builder.count(relatedRoot))
            .where(builder.and(relatedIdPredicate, relatedTenantPredicate));

    return this.entityManager.createQuery(relatedSelect).getSingleResult();
  }

  @Override
  public void deleteByTenantIdAndId(UUID tenantId, UUID id) throws NoSuchElementException {
    delete(findByTenantIdAndId(tenantId, id).orElseThrow());
  }

  @Override
  public UUID findId(T entity) {
    return (UUID) this.persistenceUnitUtil.getIdentifier(entity);
  }


  private List<Order> getOrderCriteria(Sort sort, Path<?> path, CriteriaBuilder builder) {

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
}
