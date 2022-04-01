package uk.gov.homeoffice.digital.sas.jparest.models;

import org.hibernate.proxy.HibernateProxy;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides an implementation of equals and hashCode that
 * uses the field of the subclass marked with the {@link Id} annotation.
 */
public abstract class BaseEntity {

    private static final Logger LOGGER = Logger.getLogger(BaseEntity.class.getName());
    public static final String ID_ERROR_MESSAGE = "%s should not be extended by a subclass  %s with %s number of @Id annotations";

    private Field idField = getIdField();

    /**
     * @return {@link Field} annotated with {@link Id}
     */
    @NotNull
    private Field getIdField() {
        var entityClass = this instanceof HibernateProxy ? this.getClass().getSuperclass() : this.getClass();
        List<Field> idFields = Arrays
                .stream(entityClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
        if (idFields.size()!=1) {
            throw new ResourceException(String.format(ID_ERROR_MESSAGE,BaseEntity.class.getName(), entityClass.getName(), idFields.size()));
        }
        idFields.get(0).setAccessible(true);
        return idFields.get(0);
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
            LOGGER.log(Level.SEVERE, "Error accessing ID field {} {}", new Object[]{this.idField.getName(), e});
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
        var thisId = getId();
        var thatId = getId(obj);
        return thisId != null && thatId != null && thisId == thatId;
    }
}
