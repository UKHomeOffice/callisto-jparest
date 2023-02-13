package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "artists", filterExamples = {
  @ExampleObject(name = "performanceName match", value = "performanceName matches '%Be%'")
})
@Entity(name = "artists")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class Artist extends BaseEntity {

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "profile_id")
  private UUID profileId;

  @NotEmpty
  private String performanceName;

  @OneToOne(optional = false)
  @JoinColumn(
      name = "profile_id",
      unique = true,
      nullable = false,
      insertable = false,
      updatable = false)
  @JsonIgnore
  private Profile profile;

  @OneToMany(mappedBy = "artist")
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Set<Record> records;

  @ManyToMany(mappedBy = "artists")
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Set<Concert> concerts;

}
