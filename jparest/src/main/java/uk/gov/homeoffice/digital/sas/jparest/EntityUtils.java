package uk.gov.homeoffice.digital.sas.jparest;

import lombok.Getter;
import lombok.NonNull;

import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Logger;

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
    public EntityUtils(@NonNull Class<T> entityType, @NonNull EntityManager entityManager) {

        Set<String> tmpRelatedResources = new HashSet<>();

        // Iterate the declared fields to find the field annotated with Id
        // and to find the fields markerd ManyToMany
        String tmpIdFieldName = null;
        Class<?> tmpIdFieldType = null;
        Field tmpIdField = null;
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                tmpIdField = field;
                tmpIdField.setAccessible(true); //NOSONAR
                tmpIdFieldName = field.getName();
                tmpIdFieldType = field.getType();
            }

            // Only record relationships that aren't mapped by another class
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany m2m = field.getAnnotation(ManyToMany.class);
                if (!StringUtils.hasText(m2m.mappedBy())) {
                    var relatedEntityType = field.getGenericType();
                    if (relatedEntityType instanceof ParameterizedType) {
                        relatedEntityType = ((ParameterizedType) relatedEntityType).getActualTypeArguments()[0];
                    }

                    EntityType<?> ret = entityManager.getMetamodel().entity((Class<?>) relatedEntityType);
                    Class<?> relatedIdType = ret.getIdType().getJavaType();
                    var relatedIdField = (Field) ret.getDeclaredId((Class<?>) relatedIdType).getJavaMember();
                    field.setAccessible(true); //NOSONAR
                    relations.putIfAbsent(field.getName(), new RelatedEntity(field, (Class<?>) relatedEntityType, relatedIdType, relatedIdField));
                    tmpRelatedResources.add(field.getName());
                }
            }

        }

        this.entityType = entityType;
        this.relatedResources = tmpRelatedResources;
        this.idField = tmpIdField;
        this.idFieldType = tmpIdFieldType;
        this.idFieldName = tmpIdFieldName;
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
    public Collection<Object> getRelatedEntities(Object entity, String relation) {
        var relatedEntity = this.relations.get(relation);
        Collection<Object> result = null;
        try {
            result = (Collection<Object>) relatedEntity.declaredField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.severe("Unable to access " + relation + " of entity type " + entity.getClass().getName());
        }
        return result;
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
    private Object getEntityReference(Class<?> entityType, Field idField, Serializable identifier) throws IllegalArgumentException {
        Object reference = null;
        try {
            reference = entityType.getConstructor(new Class<?>[]{}).newInstance();
            idField.set(reference, identifier); //NOSONAR
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
    public Object getEntityReference(Serializable identifier) {
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
