package uk.gov.homeoffice.digital.sas.jparest.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.type.SqlTypes;

/**
 * Provides an implementation of equals and hashCode that
 * uses the field of the subclass marked with the {@link Id} annotation.
 */
@MappedSuperclass
public abstract class BaseEntity {

  @Getter
  @Setter
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
      name = "UUID",
      strategy = "org.hibernate.id.UUIDGenerator",
      parameters = @Parameter(name = "uuid_gen_strategy_class",
      value = "org.hibernate.id.uuid.CustomVersionOneStrategy"))
  @JdbcTypeCode(SqlTypes.CHAR)
  protected UUID id;

  @Getter
  @Setter
  @JdbcTypeCode(SqlTypes.CHAR)
  protected UUID tenantId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    if (!(o instanceof BaseEntity)) {
      return false;
    }
    BaseEntity that = (BaseEntity) o;
    return (null != getId() && null != that.getId() && getId().equals(that.getId()));

  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
