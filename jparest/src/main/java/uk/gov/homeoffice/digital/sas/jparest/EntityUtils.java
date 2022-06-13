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
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

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
    public static final String ID_FIELD_NAME = "id";

    @Getter
    private Class<T> entityType;
    @Getter
    private Set<String> relatedResources = new HashSet<>();
    private Field idField = getBaseEntityIdField1();
    @Getter
    private Class<?> idFieldType= UUID.class;
    @Getter
    private String idFieldName = ID_FIELD_NAME;
    private Map<String, RelatedEntity> relations = new HashMap<>();
    private EntityManager entityManager;

    /**
     * Creates a utility class for the specified entityType
     *
     * @param entityType    The JPA entity class
     * @param entityManager The {@link EntityManager}
     */
    @SuppressWarnings("squid:S3011") // Need to set accessibility of field to create instances with id set without
                                     // touching the database
    public EntityUtils(@NonNull Class<T> entityType, @NonNull EntityManager entityManager) {

        this.entityManager = entityManager;
        this.entityType = entityType;

        // Iterate the declared fields to find the fields marked ManyToMany
        // Only record relationships that aren't mapped by another class
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class) && !StringUtils.hasText(field.getAnnotation(ManyToMany.class).mappedBy())) {
                field.setAccessible(true);
                relations.putIfAbsent(field.getName(), getRelatedEntity(field));
                relatedResources.add(field.getName());
            }
        }
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

    public RelatedEntity getRelatedEntity(Field declaredField) {
        // Validate the related Entity is of BaseEntity type
        validateEntityIsOfBaseEntityType((Class<?>) getRelatedEntityType(declaredField));
        var relatedEntityType = (Class<?>) getRelatedEntityType(declaredField);
        Class<?> idFieldType1 = UUID.class;
        var idField = getBaseEntityIdField(declaredField);
        // TODO need to replace it with direct access method later
        //var idField = getBaseEntityIdField1();
        return new RelatedEntity(declaredField, relatedEntityType, idFieldType1, idField);
    }

    // TODO This method should be replaced with the getBaseEntityIdField1() after fixing the Reference
    private Field getBaseEntityIdField(Field declaredField) {
        Class<?> relatedEntityType = (Class<?>) getRelatedEntityType(declaredField);
        EntityType<?> ret = entityManager.getMetamodel().entity((Class<?>) relatedEntityType);
        Class<?> relatedIdType = ret.getIdType().getJavaType();
        var idField = (Field) ret.getId((Class<?>)  UUID.class).getJavaMember();
        idField.setAccessible(true);
        return idField;
    }

    private Field getBaseEntityIdField1() {
        Field idField;
        try {
            idField = BaseEntity.class.getDeclaredField(ID_FIELD_NAME);
        } catch (NoSuchFieldException e) {
            LOGGER.severe(ID_FIELD_NAME + " not declared in this Entity " + BaseEntity.class.getName());
            throw new ResourceException(ID_FIELD_NAME + " not declared in this Entity " + BaseEntity.class.getName());
        }
        // TODO This need to be removed after enabling direct access to id field
        idField.setAccessible(true);
        return idField;
    }

    // Validate the Related entity also inherits from the BaseEntity
    public boolean validateEntityIsOfBaseEntityType(Class<?> entityType){
        return (entityType.getClass().isInstance(BaseEntity.class));
        /*if ( !(entityType.getClass().isInstance(BaseEntity.class))) {
            throw new ResourceException( entityType + " is not inheriting from the BaseEntity");
        }*/
    }

    private Type getRelatedEntityType(Field field) {
        var relatedEntityType = field.getGenericType();
        if (relatedEntityType instanceof ParameterizedType parameterizedType) {
            relatedEntityType = parameterizedType.getActualTypeArguments()[0];
        }
        return relatedEntityType;
    }
}
