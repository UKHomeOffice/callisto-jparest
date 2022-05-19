package uk.gov.homeoffice.digital.sas.jparest.models;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceException;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation of equals and hashCode that
 * uses the field of the subclass marked with the {@link Id} annotation.
 */
@MappedSuperclass
public abstract class BaseEntity {

    private static final Logger LOGGER = Logger.getLogger(BaseEntity.class.getName());
    public static final String ID_ERROR_MESSAGE = "%s should not be extended by a subclass  %s with %s number of @Id annotations";

    @Getter
    @Setter
    @Id
    @Type(type="uuid-char")
    private UUID id;

    @Getter
    @Setter
    @Type(type="uuid-char")
    private UUID tenantId;

    /**
     * Disabling this more than one Id field validation as it is done now in Entityutils as part of EAHW-1851,
     * TODO: remove the commented code after the discussion with team
     * @return {@link Field} annotated with {@link Id}

    @NotNull
    // S3011: Need to be able to read id field for equals and hashcode
    // S3958: ToList is not identified as a terminal operator
    // https://community.sonarsource.com/t/s3958-stream-tolist-jdk-16-is-not-recognized-as-terminal-operator/41501
    // Suppresion can be removed when our sonar is updated
    @SuppressWarnings({"squid:S3011", "squid:S3958"})
    private Field getIdField() {
        var entityClass = this instanceof HibernateProxy ? this.getClass().getSuperclass() : this.getClass();
        List<Field> idFields = Arrays
                .stream(entityClass.getDeclaredFields())
                .filter(e -> e.isAnnotationPresent(Id.class))
                .toList();
        if (idFields.size() > 0) {
            throw new ResourceException(String.format(ID_ERROR_MESSAGE, BaseEntity.class.getName(),
                    entityClass.getName(), idFields.size()+1));
        }
        idFields.get(0).setAccessible(true);
        return idFields.get(0);
    }
     */


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return (null!=getId() && null!=that.getId() && getId().equals(that.getId())) &&
                (null!=getTenantId() && null!=that.getTenantId() && getTenantId().equals(that.getTenantId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTenantId());
    }
}
