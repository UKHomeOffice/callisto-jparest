package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.SpelExpressionToPredicateConverter;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;

// TODO: Added include for related materials and also add metadata to response e.g. next link

/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 * <p>
 * Resources for ManyToMany relationships can also be queried.
 */
@ResponseBody
public class ResourceApiController<T, U> {

    private EntityManager entityManager;
    private PlatformTransactionManager transactionManager;
    private JpaRepository<T, Serializable> repository;
    private EntityUtils<T> entityUtils;

    private static WebDataBinder binder = initBinder();

    private static WebDataBinder initBinder() {
        WebDataBinder binder = new WebDataBinder(null);

        StdDateFormat dateFormat2 = new StdDateFormat();
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));

        return binder;
    }

    private Serializable getIdentifier(Object identifier) {
        return getIdentifier(identifier, this.entityUtils.getIdFieldType());
    }

    private static Serializable getIdentifier(Object identifier, Class<?> fieldType) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be null");
        }
        return (Serializable) binder.convertIfNecessary(identifier, fieldType);
    }

    @ExceptionHandler({InvalidFilterException.class})
    public ResponseEntity<String> handleException(Exception ex) {
        String message = null;
        if (ex instanceof InvalidFilterException) {
            message = ex.getMessage();
        }
        // TODO: Structure this into a nice json response
        return new ResponseEntity<>(message, null, HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    public ResourceApiController(Class<T> entityType, EntityManager entityManager,
                                 PlatformTransactionManager transactionManager, EntityUtils<?> entityUtils) {
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
        this.repository = new SimpleJpaRepository<T, Serializable>(entityType, entityManager);
        this.entityUtils = (EntityUtils<T>) entityUtils;
    }

    public ApiResponse<T> list(SpelExpression filter, Pageable pageable) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        Predicate predicate = SpelExpressionToPredicateConverter.convert(filter, builder, root);
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
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);
        List<T> result = typedQuery.getResultList();
        return new ApiResponse<>(result);
    }

    private T getById(U id, String include) {
        Serializable identifier = getIdentifier(id);

        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
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
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);
        List<T> result2 = typedQuery.getResultList();
        if (result2.isEmpty()) {
            return null;
        }
        return result2.get(0);
    }

    public ResponseEntity<ApiResponse<T>> get(@PathVariable U id) {
        T result = getById(id, null);
        if (result == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }
        ApiResponse<T> results = new ApiResponse<T>(Arrays.asList(result));
        return new ResponseEntity<ApiResponse<T>>(results, null, HttpStatus.OK);
    }

    // TODO: Test invalid json payloads
    public ResponseEntity<ApiResponse<T>> create(@RequestBody String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        T r2;
        try {
            r2 = objectMapper.readValue(body, this.entityUtils.getEntityType());
        } catch (JsonProcessingException ex) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
        T result;
        try {
            result = repository.saveAndFlush((T) r2);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
        ApiResponse<T> results = new ApiResponse<T>(Arrays.asList(result));
        return new ResponseEntity<ApiResponse<T>>(results, HttpStatus.OK);
    }

    public ResponseEntity<String> delete(@PathVariable U id) {
        Serializable identifier = getIdentifier(id);

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
        try {
            repository.deleteById(identifier);
            transactionManager.commit(transactionStatus);
        } catch (EmptyResultDataAccessException ex) {
            transactionManager.rollback(transactionStatus);
            return new ResponseEntity<String>(null, null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }
        return new ResponseEntity<String>(null, null, HttpStatus.OK);
    }

    public ResponseEntity<ApiResponse<T>> update(@PathVariable U id, @RequestBody String body)
            throws JsonProcessingException {

        Serializable identifier = getIdentifier(id);

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        ObjectMapper objectMapper = new ObjectMapper();
        T r2;
        try {
            r2 = objectMapper.readValue(body, this.entityUtils.getEntityType());
        } catch (JsonProcessingException ex) {
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        }
        T orig;
        Optional<T> result = repository.findById(identifier);
        // TODO: Check id on incoming resource and make sure it matches
        if (result.isEmpty()) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        } else {
            orig = result.get();
        }
        BeanUtils.copyProperties(r2, orig, this.entityUtils.getIdFieldName());
        try {
            repository.saveAndFlush((T) orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        ApiResponse<T> results = new ApiResponse<T>(Arrays.asList((T) orig));
        return new ResponseEntity<ApiResponse<T>>(results, null, HttpStatus.OK);
    }

    public ApiResponse<?> getRelated(@PathVariable U id, @PathVariable String relation,
                                     SpelExpression filter, Pageable pageable) {
        Serializable identifier = getIdentifier(id);

        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        Class<?> entityType = this.entityUtils.getRelatedType(relation);
        CriteriaQuery<?> query = builder.createQuery(entityType);
        Root<T> root = query.from(this.entityUtils.getEntityType());
        CriteriaQuery<?> select = query.select(root.join(relation));
        Join<?, ?> relatedJoin = root.getJoins().iterator().next();

        Predicate predicate = builder.equal(root, identifier);
        Predicate filterPredicate = SpelExpressionToPredicateConverter.convert(filter, builder, relatedJoin);
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
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);

        List<?> result = typedQuery.getResultList();
        return new ApiResponse<>(result);
    }

    public ResponseEntity<String> deleteRelated(@PathVariable U id, @PathVariable String relation,
                                                @PathVariable Object[] related_id)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        T orig = getById(id, relation);

        // TODO: Check id on incoming resource and make sure it matches
        if (orig == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }

        Collection<?> relatedEntities = entityUtils.getRelatedEntities(orig, relation);
        Class<?> related_id_type = entityUtils.getRelatedIdType(relation);

        for (Object object : related_id) {
            Serializable identitfier = getIdentifier(object, related_id_type);
            // TODO: Check for null here also on the add related
            Object f = this.entityUtils.getEntityReference(relation, identitfier);
            if (!relatedEntities.remove(f)) {
                return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
            }
        }

        try {
            repository.saveAndFlush((T) orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        return new ResponseEntity<String>(null, null, HttpStatus.OK);
    }

    @SuppressWarnings(value = {"rawtypes", "unchecked"}) //
    public ResponseEntity<String> addRelated(@PathVariable U id, @PathVariable String relation,
                                             @PathVariable Object[] related_id)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(transactionDefinition);

        T orig = getById(id, relation);

        // TODO: Check id on incoming resource and make sure it matches
        if (orig == null) {
            return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
        }

        Collection relatedEntities = entityUtils.getRelatedEntities(orig, relation);
        Class<?> related_id_type = entityUtils.getRelatedIdType(relation);

        for (Object object : related_id) {
            Serializable identifier = getIdentifier(object, related_id_type);
            Object f = this.entityUtils.getEntityReference(relation, identifier);
            if (!relatedEntities.contains(f)) {
                relatedEntities.add(f);
            }
        }

        try {
            repository.saveAndFlush((T) orig);
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException ex) {
            transactionManager.rollback(transactionStatus);
            throw ex;
        }

        return new ResponseEntity<String>(null, null, HttpStatus.OK);
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
