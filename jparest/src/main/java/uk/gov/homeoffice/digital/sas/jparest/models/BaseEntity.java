package uk.gov.homeoffice.digital.sas.jparest.models;

import org.hibernate.proxy.HibernateProxy;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private Field getIdField() {

        var entityClass = this instanceof HibernateProxy ? this.getClass().getSuperclass() : this.getClass();
        for (Field field : entityClass.getDeclaredFields()) {
            if (idField == null && field.isAnnotationPresent(Id.class)) {
                idField = field;
                idField.setAccessible(true); //NOSONAR

            } else if (field.isAnnotationPresent(Id.class)) {
                throw new ResourceException(String.format(
                        "%s should not be extended by a subclass with multiple id annotations", BaseEntity.class.getName()));
            }
        }

        if (idField == null) throw new ResourceException(String.format(
                "%s should not be extended by subclasses that do not have the id annotation", BaseEntity.class.getName()));

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
