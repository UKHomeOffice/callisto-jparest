package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;


import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.utils.WebDataBinderFactory;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;

/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
@ResponseBody
public class ResourceApiController<T, U> {

    @Getter
    private Class<T> entityType;
    private EntityManager entityManager;
    private PersistenceUnitUtil persistenceUnitUtil;
    private PlatformTransactionManager transactionManager;
    private JpaRepository<T, Serializable> repository;
    private EntityUtils<T> entityUtils;

    private static WebDataBinder binder = WebDataBinderFactory.getWebDataBinder();
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

    @ExceptionHandler({InvalidFilterException.class})
    public ResponseEntity<String> handleException(InvalidFilterException ex) {
        return new ResponseEntity<>(ex.getMessage(), null, HttpStatus.BAD_REQUEST);
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

    public ApiResponse<T> list(SpelExpression filter, Pageable pageable) {
        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        var predicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
        if (filter != null) {
            query.where(predicate);
        }

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

    private T getById(U id, String include) {
        Serializable identifier = getIdentifier(id);

        var builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        Predicate filter = builder.equal(root, identifier);
        query.where(filter);

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
        if (StringUtils.hasText(include)) {
            entityGraph.addAttributeNodes(include);
        }

        CriteriaQuery<T> select = query.select(root);
        TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setHint(QUERY_HINT, entityGraph);
        List<T> result2 = typedQuery.getResultList();
        if (result2.isEmpty()) {
            return null;
        }
        return result2.get(0);
    }

    public ResponseEntity<ApiResponse<T>> get(@PathVariable U id) {
        var result = getById(id, null);
        if (result == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }
        var results = new ApiResponse<>(Arrays.asList(result));
        return new ResponseEntity<>(results, null, HttpStatus.OK);
    }

    public ResponseEntity<ApiResponse<T>> create(@RequestBody String body) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        T r2;
        try {
            r2 = objectMapper.readValue(body, this.entityUtils.getEntityType());
        } catch (JsonProcessingException ex) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
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
        ApiResponse<T> results = new ApiResponse<>(Arrays.asList(result));
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    public ResponseEntity<Void> delete(@PathVariable U id) {
        var identifier = getIdentifier(id);

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
        try {
            repository.deleteById(identifier);
            transactionManager.commit(transactionStatus);
        } catch (EmptyResultDataAccessException ex) {
            transactionManager.rollback(transactionStatus);
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }

    public ResponseEntity<ApiResponse<T>> update(@PathVariable U id, @RequestBody String body)
            throws JsonProcessingException {

        var identifier = getIdentifier(id);

        var objectMapper = new ObjectMapper();
        T r2;
        try {
            r2 = objectMapper.readValue(body, this.entityUtils.getEntityType());
        } catch (JsonProcessingException ex) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }

        var payloadEntityId = this.persistenceUnitUtil.getIdentifier(r2);
        if (payloadEntityId != null && identifier != getIdentifier(payloadEntityId)) {
            throw new IllegalArgumentException("The supplied payload resource id value must match the url id path parameter value");
        }

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        T orig;
        Optional<T> result = repository.findById(identifier);

        if (result.isEmpty()) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        } else {
            orig = result.get();
        }
        BeanUtils.copyProperties(r2, orig, this.entityUtils.getIdFieldName());
        try {
            repository.saveAndFlush(orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        ApiResponse<T> results = new ApiResponse<>(Arrays.asList(orig));
        return new ResponseEntity<>(results, null, HttpStatus.OK);
    }

    @SuppressWarnings("rawtypes")
    public ApiResponse getRelated(
            @PathVariable U id, @PathVariable String relation,
            SpelExpression filter, Pageable pageable) {
        Serializable identifier = getIdentifier(id);
        var builder = this.entityManager.getCriteriaBuilder();
        Class<?> relatedEntityType = this.entityUtils.getRelatedType(relation);
        CriteriaQuery<?> query = builder.createQuery(relatedEntityType);
        Root<T> root = query.from(this.entityUtils.getEntityType());
        CriteriaQuery<?> select = query.select(root.join(relation));
        Join<?, ?> relatedJoin = root.getJoins().iterator().next();

        var predicate = builder.equal(root, identifier);
        var filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, relatedJoin);
        if (filterPredicate != null) {
            predicate = builder.and(predicate, filterPredicate);
        }
        select.where(predicate);

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

    public ResponseEntity<String> deleteRelated(@PathVariable U id, @PathVariable String relation,
                                                @PathVariable Object[] relatedId)
            throws IllegalArgumentException {

        var orig = getById(id, relation);

        if (orig == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        Collection<?> relatedEntities = entityUtils.getRelatedEntities(orig, relation);
        Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);

        for (Object object : relatedId) {
            Serializable identitfier = getIdentifier(object, relatedIdType);
            Object f = this.entityUtils.getEntityReference(relation, identitfier);
            if (!relatedEntities.remove(f)) {
                return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
            }
        }

        try {
            repository.saveAndFlush(orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        return new ResponseEntity<>(null, null, HttpStatus.OK);
    }

    public ResponseEntity<String> addRelated(@PathVariable U id, @PathVariable String relation,
                                             @PathVariable Object[] relatedId)
            throws IllegalArgumentException {

        var orig = getById(id, relation);

        if (orig == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }

        var transactionDefinition = new DefaultTransactionDefinition();
        var transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        Collection<Object> relatedEntities = entityUtils.getRelatedEntities(orig, relation);
        Class<?> relatedIdType = entityUtils.getRelatedIdType(relation);

        for (Object object : relatedId) {
            Serializable identifier = getIdentifier(object, relatedIdType);
            Object f = this.entityUtils.getEntityReference(relation, identifier);
            if (!relatedEntities.contains(f)) {
                relatedEntities.add(f);
            }
        }

        try {
            repository.saveAndFlush(orig);
            transactionManager.commit(transactionStatus);
        } catch (EntityNotFoundException ex){
            transactionManager.rollback(transactionStatus);
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        return new ResponseEntity<>(null, null, HttpStatus.OK);
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
}
