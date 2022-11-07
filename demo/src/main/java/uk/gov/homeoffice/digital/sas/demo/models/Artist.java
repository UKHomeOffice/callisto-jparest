package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
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
@EqualsAndHashCode
public class Artist extends BaseEntity {

  @Type(type = "uuid-char")
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
  private Set<Record> records;

  @ManyToMany(mappedBy = "artists")
  @JsonIgnore
  private Set<Concert> concerts;

}
