package uk.gov.homeoffice.digital.sas.jparest;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.metamodel.EntityType;

import org.springframework.util.StringUtils;

import lombok.Getter;

/**
 * Provides utility functions for JPA entities.
 * It provides the entity's id field and entity
 * information for any {@link ManyToMany} mapping 
 * declared in the entity
 * i.e. it excludes {@link ManyToMany} with the 
 * mappedBy property set.
 */
class EntityUtils<T> {

    private final static Logger LOGGER = Logger.getLogger(EntityUtils.class.getName());

    @Getter
    private Class<T> entityType;
    @Getter
    private Set<String> relatedResources = new HashSet<String>();
    private Field idField;
    @Getter
    private Class<?> idFieldType;
    @Getter
    private String idFieldName;
    private Map<String, RelatedEntity> relations = new HashMap<String, RelatedEntity>();

    /**
     * Creates a utility class for the specified entityType
     * @param entityType The JPA entity class
     * @param entityManager The {@link EntityManager}
     */
    EntityUtils(Class<T> entityType, EntityManager entityManager){
        
        Set<String> relatedResources = new HashSet<String>();

        // Iterate the declared fields to find the field annotated with Id
        // and to find the fields markerd ManyToMany
        String idFieldName = null;
        Class<?> idFieldType = null;
        Field idField = null;
        for (Field field : entityType.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                idFieldName = field.getName();
                idFieldType = field.getType();
            }
            
            // Only record relationships that aren't mapped by another class
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany m2m = field.getAnnotation(ManyToMany.class);
                if (!StringUtils.hasText(m2m.mappedBy())) {
                    Type relatedEntityType = field.getGenericType();
                    if (relatedEntityType instanceof ParameterizedType) {
                        relatedEntityType = ((ParameterizedType)relatedEntityType).getActualTypeArguments()[0];
                    }

                    EntityType<?> ret = entityManager.getMetamodel().entity((Class<?>)relatedEntityType);
                    Class<?> related_id_type = ret.getIdType().getJavaType();
                    Field related_id_field = (Field)ret.getDeclaredId((Class<?>)related_id_type).getJavaMember();
                    field.setAccessible(true);
                    relations.putIfAbsent(field.getName(), new RelatedEntity(field, (Class<?>)relatedEntityType, related_id_type, related_id_field));
                    relatedResources.add(field.getName());
                }
            }
            
        }

        this.entityType = entityType;
        this.relatedResources = relatedResources;
        this.idField = idField;
        this.idFieldType = idFieldType;
        this.idFieldName = idFieldName;
    }

    /**
     * Returns the collection of entities decribed by the given 
     * relation.
     * 
     * @param entity The entity to retrieve related entities from
     * @param relation The name of the property that is annotated 
     * with ManyToMany
     * @return Collection of entities expressed by the ManyToMany attribute
     */
    Collection<?> getRelatedEntities(Object entity, String relation ) {
        RelatedEntity relatedEntity = this.relations.get(relation);
        Collection<?> result = null;
        try {
            result = (Collection<?>)relatedEntity.declaredField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LOGGER.severe("Unable to access " + relation + " of entity type " + entity.getClass().getName());
        }
        return result;
    }
    
    /**
     * Creates an instance of an entity with it's {@link Id} attributed field
     * set to the given identifier.
     * 
     * This is used as an optimisation to the {@link EntityManager.getReference}
     * as it doesn't require a call to the database as it'a only intended to facilitate
     * adding and removing related entities in a ManyToMany relationship.
     * @param entityType The type of entity to create a reference of
     * @param idField The field of the entity type that is attributed with {@link Id}
     * @param identifier The value of the Id.
     * @return An instance of entityType with the idField set to the identifier
     */
    private Object getEntityReference(Class<?> entityType, Field idField, Serializable identifier) {
        Object reference = null;
        try {
            reference = entityType.getConstructor(new Class<?>[] {}).newInstance(new Object[] {});
            idField.set(reference, identifier);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.severe("Unable to create reference for entity " + entityType.getName());
        }
        return reference;

    }

    /**
     * Creates a reference for the entity type this utility class is for
     * @param identifier The value to set the Id to
     * @return An instance of the entityType represented by the utility class
     * with its identifier set to the given value
     */
    Object getEntityReference(Serializable identifier) {
        return getEntityReference(this.entityType, this.idField, identifier);
    }

    /**
     * Creates an reference entity for the entityType exposed by the specified 
     * relation.
     */
    Object getEntityReference(String relation, Serializable identifier) {
        RelatedEntity relatedEntity = this.relations.get(relation);
        return getEntityReference(relatedEntity.entityType, relatedEntity.idField, identifier);
    }

    /**
     * Provides the type of the Id field for the entity accessed by the specified relation
     */
    Class<?> getRelatedIdType(String relation) {
        return this.relations.get(relation).idFieldType;
    }

    /**
     * Provides the Id field for the entity accessed by the specified relation
     */
    Field getRelatedIdField(String relation) {
        return this.relations.get(relation).idField;
    }

    class RelatedEntity {
        RelatedEntity( Field declaredField, Class<?> entityType, Class<?> idFieldType, Field idField) {
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
