package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "concerts")
@Entity(name = "concerts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Concert extends BaseEntity {

  @NotEmpty
  private String concertName;

  @ManyToMany
  @JoinTable(
      name = "concert_artists",
      joinColumns = @JoinColumn(name = "concert_id"),
      inverseJoinColumns = @JoinColumn(name = "artist_id"))
  @JsonIgnore
  private Set<Artist> artists;

}
