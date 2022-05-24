package uk.gov.homeoffice.digital.sas.jparest.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;
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
