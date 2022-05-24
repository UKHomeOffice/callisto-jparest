package uk.gov.homeoffice.digital.sas.jparest;

import lombok.Getter;
import lombok.NonNull;

import org.springframework.util.StringUtils;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * Provides utility functions for JPA entities.
 * It provides the entity's id field and entity
 * information for any {@link ManyToMany} mapping
 * declared in the entity
 * i.e. it excludes {@link ManyToMany} with the
 * mappedBy property set.
 */
public class EntityUtils<T> {

    private static final Logger LOGGER = Logger.getLogger(EntityUtils.class.getName());
    public static final String MORE_THAN_ONE_ID_FIELD = "'%s' entity should not have more than one @Id field";

    @Getter
    private Class<T> entityType;
    @Getter
    private Set<String> relatedResources = new HashSet<>();
    private Field idField;
    @Getter
    private Class<?> idFieldType;
    @Getter
    private String idFieldName;
    private Map<String, RelatedEntity> relations = new HashMap<>();

    /**
     * Creates a utility class for the specified entityType
     *
     * @param entityType    The JPA entity class
     * @param entityManager The {@link EntityManager}
     */
    @SuppressWarnings("squid:S3011") // Need to set accessibility of field to create instances with id set without
                                     // touching the database
    public EntityUtils(@NonNull Class<T> entityType, @NonNull EntityManager entityManager) {

        Set<String> tmpRelatedResources = new HashSet<>();

        // Iterate the declared fields to find the field annotated with Id
        // and to find the fields marked ManyToMany
        for (Field field : entityType.getDeclaredFields()) {
            // Only record relationships that aren't mapped by another class
            if (field.isAnnotationPresent(ManyToMany.class) &&
                    !StringUtils.hasText(field.getAnnotation(ManyToMany.class).mappedBy())) {
                var relatedEntityType = field.getGenericType();
                if (relatedEntityType instanceof ParameterizedType parameterizedType) {
                    relatedEntityType = parameterizedType.getActualTypeArguments()[0];
                }

                EntityType<?> ret = entityManager.getMetamodel().entity((Class<?>) relatedEntityType);
                Class<?> relatedIdType = ret.getIdType().getJavaType();
                var relatedIdField = (Field) ret.getId((Class<?>) relatedIdType).getJavaMember();
                field.setAccessible(true);
                relations.putIfAbsent(field.getName(), new RelatedEntity(field, (Class<?>) relatedEntityType, relatedIdType, relatedIdField));
                tmpRelatedResources.add(field.getName());
            }

        }

        // Validate the number of ID fields in BaseEntity to have 1 & Entity to have 0
        List<Field> baseClassIdFields = Arrays.stream(entityType.getSuperclass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
        if(baseClassIdFields.size() > 1) {
            throw new ResourceException(format(MORE_THAN_ONE_ID_FIELD, BaseEntity.class.getName()));
        }
        Arrays.stream(entityType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findAny()
                .ifPresent( (Field e) -> {
                    throw new ResourceException(format(MORE_THAN_ONE_ID_FIELD, entityType.getName()));
                });

        this.entityType = entityType;
        this.relatedResources = tmpRelatedResources;
        this.idField = baseClassIdFields.get(0);
        idField.setAccessible(true);
        this.idFieldType = idField.getType();
        this.idFieldName = idField.getName();
    }

    /**
     * Returns the collection of entities decribed by the given
     * relation.
     *
     * @param entity   The entity to retrieve related entities from
     * @param relation The name of the property that is annotated
     *                 with ManyToMany
     * @return Collection of entities expressed by the ManyToMany attribute
     */
    public Collection<Object> getRelatedEntities(@NonNull T entity, @NonNull String relation) {
        var relatedEntity = this.relations.get(relation);
        if (relatedEntity == null) {
            throw new IllegalArgumentException(String.format("Relation '%s' does not exist", relation));
        }
        try {
            @SuppressWarnings("unchecked")
            var result = (Collection<Object>) relatedEntity.declaredField.get(entity);
            return result;
        } catch (IllegalAccessException e) {
            LOGGER.severe("Unable to access " + relation + " of entity type " + entity.getClass().getName());
        }
        return emptyList();
    }

    /**
     * Creates an instance of an entity with it's {@link Id} attributed field
     * set to the given identifier.
     * <p>
     * This is used as an optimisation to the {@link EntityManager.getReference}
     * as it doesn't require a call to the database as it'a only intended to facilitate
     * adding and removing related entities in a ManyToMany relationship.
     *
     * @param entityType The type of entity to create a reference of
     * @param idField    The field of the entity type that is attributed with {@link Id}
     * @param identifier The value of the Id.
     * @return An instance of entityType with the idField set to the identifier
     */
    @SuppressWarnings("squid:S3011") // Need to set accessibility of field to create instances with id set without
                                     // touching the database
    private static <Y> Y getEntityReference(Class<Y> entityType, Field idField, Serializable identifier) throws IllegalArgumentException {
        Y reference = null;
        try {
            reference = entityType.getConstructor().newInstance();
            idField.set(reference, identifier);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.severe("Unable to create reference for entity " + entityType.getName());
        }
        return reference;

    }

    /**
     * Creates a reference for the entity type this utility class is for
     *
     * @param identifier The value to set the Id to
     * @return An instance of the entityType represented by the utility class
     * with its identifier set to the given value
     */
    public T getEntityReference(Serializable identifier) {
        return getEntityReference(this.entityType, this.idField, identifier);
    }

    /**
     * Creates an reference entity for the entityType exposed by the specified
     * relation.
     */
    public Object getEntityReference(String relation, Serializable identifier) {
        var relatedEntity = this.relations.get(relation);
        return getEntityReference(relatedEntity.entityType, relatedEntity.idField, identifier);
    }

    /**
     * Provides the type of the entity accessed by the specified relation
     */
    public Class<?> getRelatedType(String relation) {
        return this.relations.get(relation).entityType;
    }

    /**
     * Provides the type of the Id field for the entity accessed by the specified relation
     */
    public Class<?> getRelatedIdType(String relation) {
        return this.relations.get(relation).idFieldType;
    }

    /**
     * Provides the Id field for the entity accessed by the specified relation
     */
    public Field getRelatedIdField(String relation) {
        return this.relations.get(relation).idField;
    }

    class RelatedEntity {
        RelatedEntity(Field declaredField, Class<?> entityType, Class<?> idFieldType, Field idField) {
            this.declaredField = declaredField;
            this.entityType = entityType;
            this.idFieldType = idFieldType;
            this.idField = idField;
        }

        Field declaredField;
        Class<?> entityType;
        Class<?> idFieldType;
        Field idField;
    }
}
