package uk.gov.homeoffice.digital.sas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.homeoffice.digital.sas.kafka.listener.ProfileKafkaEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "profile")
@EntityListeners(ProfileKafkaEntityListener.class)
public class Profile {
  @Id
  private String id;
  private String name;
}
