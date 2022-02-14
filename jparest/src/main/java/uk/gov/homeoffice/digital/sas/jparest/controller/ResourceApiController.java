package uk.gov.homeoffice.digital.sas.jparest.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.criteria.Criteria;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiRequestParams;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

// TODO: Added include for related materials and also add metadata to response e.g. next link
/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 * 
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

        // DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        // binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat,
        // true));// CustomDateEditor is a custom date editor
        StdDateFormat dateFormat2 = new StdDateFormat();
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));// CustomDateEditor is a custom
                                                                                         // date editor

        return binder;
    }
    // private final static Logger LOGGER =
    // Logger.getLogger(ResourceApiController.class.getName());

    private static <Y> Y convert(Object value, Class<Y> clazz) {
        return binder.convertIfNecessary(value, clazz);
    }

    private Serializable getIdentifier(Object identifier) {
        return getIdentifier(identifier, this.entityUtils.getIdFieldType());
    }

    private static Serializable getIdentifier(Object identifier, Class<?> fieldType) {

        return (Serializable) binder.convertIfNecessary(identifier, fieldType);
    }

    @SuppressWarnings("unchecked")
    public ResourceApiController(Class<T> entityType, EntityManager entityManager,
            PlatformTransactionManager transactionManager, EntityUtils<?> entityUtils) {
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
        this.repository = new SimpleJpaRepository<T, Serializable>(entityType, entityManager);
        this.entityUtils = (EntityUtils<T>) entityUtils;
    }

    public ApiResponse<T> list(ApiRequestParams params) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        Predicate filter = null;
        for (Criteria criteria : params.getCriteria()) {
            Predicate additionalPredicate = convertToPredicate(criteria, builder, root);
            if (filter != null) {
                filter = builder.and(filter, additionalPredicate);
            } else {
                filter = additionalPredicate;
            }
        }

        if (filter != null) {
            query.where(filter);
        }

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
        // TODO: rework when refactoring for filter is done
        // for (var entry : params.getRequestParams().entrySet()) {
        // String key = entry.getKey();
        // if (key.equals("include")) {
        // for (var include : entry.getValue().split(",")) {
        // if (StringUtils.hasText(include)) {
        // // entityGraph.addSubgraph(include, null);
        // entityGraph.addAttributeNodes(include);
        // }
        // }
        // }
        // }

        CriteriaQuery<T> select = query.select(root);
        TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setFirstResult((params.getPage() - 1) * params.getPageSize());
        typedQuery.setMaxResults(params.getPageSize());
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);
        List<T> result = typedQuery.getResultList();
        return new ApiResponse<>(result);
    }

    // public ResponseEntity<String> invalidIncludes(@RequestParam Map<String,
    // String> params) {

    // var include = params.get("include");
    // return new ResponseEntity<>("include querystring parameter contains invalid
    // values " + include, null,
    // HttpStatus.BAD_REQUEST);

    // }

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

    public ApiResponse<T> getRelated(@PathVariable U id, @PathVariable String relation,
            ApiRequestParams params) {
        Serializable identifier = getIdentifier(id);

        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(this.entityUtils.getEntityType());
        Root<T> root = query.from(this.entityUtils.getEntityType());

        Predicate filter = builder.equal(root, identifier);
        query.where(filter);

        EntityGraph<T> entityGraph = this.entityManager.createEntityGraph(this.entityUtils.getEntityType());
        @SuppressWarnings("unchecked")
        CriteriaQuery<T> select = query.select((Path<T>) root.get(relation));
        TypedQuery<T> typedQuery = this.entityManager.createQuery(select);
        typedQuery.setFirstResult((params.getPage() - 1) * params.getPageSize());
        typedQuery.setMaxResults(params.getPageSize());
        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);

        List<T> result = typedQuery.getResultList();
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

    @SuppressWarnings(value = { "rawtypes", "unchecked" }) //
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

    // TODO: This should be in a utility class or something
    private static Predicate convertToPredicate(Criteria criteria, CriteriaBuilder builder, Root<?> root) {
        Path<Object> field = root.get(criteria.getFieldName());
        Class<?> clazz = field.getJavaType();

        switch (criteria.getFunction()) {
            case Like:
                return builder.like(field.as(String.class), (String) criteria.getValue());
            case NotLike:
                return builder.notLike(field.as(String.class), (String) criteria.getValue());
            case Eq:
                return builder.equal(field, convert(criteria.getValue(), clazz));
            case Ge:
                return builder.ge(field.as(Number.class), (Number) convert(criteria.getValue(), clazz));
            case Gt:
                return builder.gt(field.as(Number.class), (Number) convert(criteria.getValue(), clazz));
            case Le:
                return builder.le(field.as(Number.class), (Number) convert(criteria.getValue(), clazz));
            case Lt:
                return builder.lt(field.as(Number.class), (Number) convert(criteria.getValue(), clazz));
            case In:
                return field.in((Object[]) criteria.getValue().split(","));
            case NotIn:
                return builder.not(field.in((Object[]) criteria.getValue().split(",")));
            case Between:
                String[] values = criteria.getValue().split(",");
                Path<Comparable<Object>> comparableField = root.get(criteria.getFieldName());
                @SuppressWarnings("unchecked")
                Predicate result = builder.between(comparableField, (Comparable<Object>) convert(values[0], clazz),
                        (Comparable<Object>) convert(values[1], clazz));
                return result;
            default:
                break;

        }

        return null;
    }

}
