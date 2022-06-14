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
public class EntityUtils<T extends BaseEntity> {

    private static final Logger LOGGER = Logger.getLogger(EntityUtils.class.getName());
    public static final String ID_FIELD_NAME = "id";
    public static final Class<UUID> ID_FIELD_TYPE = UUID.class;

    @Getter
    private Class<T> entityType;
    @Getter
    private Set<String> relatedResources = new HashSet<>();
    @Getter
    private Class<?> idFieldType= ID_FIELD_TYPE;
    @Getter
    private String idFieldName = ID_FIELD_NAME;
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
        this.entityType = entityType;
        // Iterate the declared fields to find the fields marked ManyToMany
        // Only record relationships that aren't mapped by another class
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)
                    && !StringUtils.hasText(field.getAnnotation(ManyToMany.class).mappedBy())
                    && isBaseEntityType(field) // and make sure derives from BaseEntity
            ) {
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
     * @param identifier The value of the Id.
     * @return An instance of entityType with the idField set to the identifier
     */
    @SuppressWarnings("squid:S3011") // Need to set accessibility of field to create instances with id set without
                                     // touching the database
    private static <Y extends BaseEntity> Y getEntityReference(
            Class<? extends BaseEntity> entityType,
            UUID identifier) throws IllegalArgumentException {
        Y reference = null;
        try {
            reference = (Y) entityType.getConstructor().newInstance();
            reference.setId(identifier);
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
    public T getEntityReference(UUID identifier) {
        return getEntityReference(this.entityType,identifier);
    }

    /**
     * Creates an reference entity for the entityType exposed by the specified
     * relation.
     */
    public Object getEntityReference(String relation, UUID identifier) {
        var relatedEntity = this.relations.get(relation);
        return getEntityReference(relatedEntity.entityType, identifier);
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
    public Class<UUID> getRelatedIdType(String relation) {
        return ID_FIELD_TYPE;
    }

    class RelatedEntity {
        RelatedEntity(Field declaredField, Class<T> entityType) {
            this.declaredField = declaredField;
            this.entityType = entityType;
        }
        Field declaredField;
        Class<T> entityType;
    }

    private RelatedEntity getRelatedEntity(Field declaredField) {
        var relatedEntityType = (Class<T>) getRelatedEntityType(declaredField);
        return new RelatedEntity(declaredField, relatedEntityType);
    }

    // Validate the Related entity also inherits from the BaseEntity
    public boolean isBaseEntityType(Field declaredField){
        Type relatedEntityType = getRelatedEntityType(declaredField);
        return (relatedEntityType.getClass().isInstance(BaseEntity.class));
    }

    private Type getRelatedEntityType(Field field) {
        var relatedEntityType = field.getGenericType();
        if (relatedEntityType instanceof ParameterizedType parameterizedType) {
            relatedEntityType = parameterizedType.getActualTypeArguments()[0];
        }
        return relatedEntityType;
    }
}
