package uk.gov.homeoffice.digital.sas.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

@Resource(path = "profiles")
@Entity(name = "profiles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Profile extends BaseEntity {

  @NotEmpty
  private String preferences;

  @NotEmpty
  private String bio;

  @NotEmpty
  private String phoneNumber;

  @NotNull
  private Date dob;

  @NotNull
  private Date firstRelease;


}
