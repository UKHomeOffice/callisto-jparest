package uk.gov.homeoffice.digital.sas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.homeoffice.digital.sas.kafka.listener.KafkaEntityListener;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "profile")
@EntityListeners(KafkaEntityListener.class)
public class Profile implements Messageable {

  @Id
  private String id;
  private String tenantId;
  private String name;

  @Override
  public String resolveMessageKey() {
    return getId() + ":" + getTenantId();
  }
}
