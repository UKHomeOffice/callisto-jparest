package uk.gov.homeoffice.digital.sas.jparest.models;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * Provides an implementation of equals and hashCode that
 * uses the field of the subclass marked with the {@link Id} annotation.
 */
public abstract class BaseEntity {

    private static final Logger LOGGER = Logger.getLogger(BaseEntity.class.getName());

    private Field idField = getIdField();

    /**
     * @return {@link Field} annotated with {@link Id}
     */
    private Field getIdField() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                idField.setAccessible(true); //NOSONAR
            }
        }
        // TODO: Should throw an error as this class should not be extended 
        // if the subclass will not be annotated with the Id annotation
        return idField;
    }

    private Serializable getId() {
        return getId(this);
    }

    /*
        Calling methods ensure that this method is not called
        before checking that the instance specified is a 
        matching type.
    */
    private Serializable getId(Object instance) {
        // TODO: Can be removed once constructor throws
        if (this.idField == null) {
            return null;
        }
        try {
            return (Serializable) this.idField.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // This shouldn't happen as we set it to accessible
            LOGGER.severe("Error accessing field " + this.idField.getName());
        }
        return null;
    }

    @Override
    public int hashCode() {
        Serializable id = getId();
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return getId() == getId(obj);
    }
}
