package uk.gov.homeoffice.digital.sas.jparest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.ENTITY_TENANT_ID_FIELD_NAME;


/**
 * Repository that queries entities and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
public class JpaRestRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaRestRepository<T, ID> {


    private final EntityManager entityManager;
    private final Class<T> entityType;

    private static final String ENTITY_ID_FIELD_NAME = "id";
    private static final String QUERY_HINT = "javax.persistence.fetchgraph";

    public JpaRestRepositoryImpl(Class<T> entityType, EntityManager entityManager) {
        super(entityType, entityManager);
        this.entityManager = entityManager;
        this.entityType = entityType;
    }


    @Override
    @Transactional
    public List<T> findAllByTenantId(UUID tenantId, SpelExpression filter, Pageable pageable) {

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityType);
        Root<T> root = query.from(entityType);

        var tenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
        var finalPredicate = filter != null
                ? builder.and(tenantPredicate, filterPredicate) : tenantPredicate;
        query.where(finalPredicate);

        var select = query.select(root);
        var orderBy = toOrders(pageable.getSort(), root, builder);
        select.orderBy(orderBy);

        return createQuery(select, pageable).getResultList();
    }

    @Override
    @Transactional
    public Optional<T> findByIdAndTenantId(ID id, UUID tenantId, String include) {

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityType);
        Root<T> root = query.from(entityType);

        var tenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var idPredicate = builder.equal(root.get(ENTITY_ID_FIELD_NAME), id);
        var finalPredicate = builder.and(tenantPredicate, idPredicate);
        query.where(finalPredicate);

        var results = createQuery(query.select(root), include).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    @Transactional
    public Optional<T> findByIdAndTenantId(ID id, UUID tenantId) {
        return this.findByIdAndTenantId(id, tenantId, null);
    }

    @Override
    @Transactional
    public List<?> findAllByIdAndRelationAndTenantId(ID id,
                                                     String relation,
                                                     Class<?> relatedEntityType,
                                                     UUID tenantId,
                                                     SpelExpression filter,
                                                     Pageable pageable) {

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = builder.createQuery(relatedEntityType);
        Root<T> root = query.from(entityType);
        CriteriaQuery<?> select = query.select(root.join(relation));
        var relatedJoin = root.getJoins().iterator().next();

        var idPredicate = builder.equal(root.get(ENTITY_ID_FIELD_NAME), id);
        if (filter != null) idPredicate = builder.and(
                idPredicate, SpelExpressionToPredicateConverter.convert(filter, builder, relatedJoin));
        var parentTenantPredicate = builder.equal(root.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var relatedTenantPredicate = builder.equal(relatedJoin.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var finalPredicate = builder.and(parentTenantPredicate, relatedTenantPredicate, idPredicate);
        select.where(finalPredicate);

        var orderBy = toOrders(pageable.getSort(), relatedJoin, builder);
        select.orderBy(orderBy);

        return this.entityManager.createQuery(select)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .setHint(QUERY_HINT, this.entityManager.createEntityGraph(entityType))
                .getResultList();
    }

    @Override
    public Long countAllByRelationAndTenantId(Class<?> relatedEntityType,
                                              Collection<Serializable> relatedIds,
                                              UUID tenantId) {

        var builder = this.entityManager.getCriteriaBuilder();
        var query = builder.createQuery(Long.class);
        var relatedRoot = query.from(relatedEntityType);
        var relatedIdPredicate = relatedRoot.get(ENTITY_ID_FIELD_NAME).in(relatedIds);
        var relatedTenantPredicate = builder.equal(relatedRoot.get(ENTITY_TENANT_ID_FIELD_NAME), tenantId);
        var relatedSelect = query.select(builder.count(relatedRoot)).where(builder.and(relatedIdPredicate, relatedTenantPredicate));
        return this.entityManager.createQuery(relatedSelect).getSingleResult();
    }

    @Override
    @Transactional
    public void deleteByIdAndTenantId(ID id, UUID tenantId) {
        Assert.notNull(id, "The given id must not be null!");
        delete(this.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException(id)));
    }




    private List<Order> toOrders(Sort sort, Path<?> path, CriteriaBuilder builder) {

        if (sort.isUnsorted()) return Collections.emptyList();

        Assert.notNull(path, "Path must not be null!");
        Assert.notNull(builder, "CriteriaBuilder must not be null!");

        return sort.stream().map(sortOrder ->
                        sortOrder.isAscending() ?
                        builder.asc(path.get(sortOrder.getProperty())) : builder.desc(path.get(sortOrder.getProperty())))
                .collect(Collectors.toList());
    }


    private TypedQuery<T> createQuery(CriteriaQuery<T> select) {
        var entityGraph = this.entityManager.createEntityGraph(entityType);
        return this.entityManager.createQuery(select).setHint(QUERY_HINT, entityGraph);
    }

    private TypedQuery<T> createQuery(CriteriaQuery<T> select, String include) {
        var entityGraph = this.entityManager.createEntityGraph(entityType);
        if (StringUtils.hasText(include)) entityGraph.addAttributeNodes(include);
        return this.entityManager.createQuery(select).setHint(QUERY_HINT, entityGraph);
    }

    private TypedQuery<T> createQuery(CriteriaQuery<T> select, Pageable pageable) {
        return createQuery(select).setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());
    }

}
