package uk.gov.homeoffice.digital.sas.jparest.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class TenantRepositoryImpl<T, Y extends Serializable>
        extends SimpleJpaRepository<T, Y> implements TenantRepository<T, Y> {

  private final EntityManager entityManager;
  private final Class<T> entityType;
  private final String tenantIdFieldName;

  private static final String ENTITY_ID_FIELD_NAME = "id";
  private static final String QUERY_HINT = "javax.persistence.fetchgraph";


  public TenantRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
    super(entityType, entityManager);
    this.entityManager = entityManager;
    this.entityType = entityType;
    this.tenantIdFieldName = getTenantIdEntityFieldName();
  }


  @Override
  @Transactional
  public List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(entityType);
    Root<T> root = query.from(entityType);

    var tenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
    var finalPredicate =
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
  @Transactional
  public Optional<T> findByIdAndTenantId(Y id, UUID tenantId) {
    return this.findByIdAndTenantId(id, tenantId, null);
  }

  private Optional<T> findByIdAndTenantId(Y id, UUID tenantId, String relatedResourceType) {

    CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(entityType);
    Root<T> root = query.from(entityType);

    var tenantPredicate = builder.equal(root.get(tenantIdFieldName), tenantId);
    var filterPredicate = builder.equal(root.get(ENTITY_ID_FIELD_NAME), id);
    var finalPredicate = builder.and(tenantPredicate, filterPredicate);
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

  private String getTenantIdEntityFieldName() {
    return Arrays.stream(BaseEntity.class.getDeclaredFields())
        .filter(field -> field.getName().equals("tenantId")).findFirst()
        .orElseThrow(() -> new RuntimeException(
            String.format("Unable to find the tenant id field within %s class",
                BaseEntity.class.getSimpleName())))
        .getName();
  }
}
