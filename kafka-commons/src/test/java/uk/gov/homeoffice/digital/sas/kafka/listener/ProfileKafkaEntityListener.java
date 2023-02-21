package uk.gov.homeoffice.digital.sas.kafka.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
import uk.gov.homeoffice.digital.sas.model.Profile;

public class ProfileKafkaEntityListener extends KafkaEntityListener<Profile> {

  public  ProfileKafkaEntityListener(KafkaProducerService<Profile> kafkaProducerService){
    super(kafkaProducerService);
  }

  @Override
  public String resolveMessageKey(Profile profile) {
    return profile.getId();
  }

  @PostPersist
  void sendMessageOnCreate(Profile resource) {
      super.sendKafkaMessageOnCreate(resource);
  }

  @PostUpdate
  void sendMessageOnUpdate(Profile resource) {
    super.sendKafkaMessageOnUpdate(resource);
  }

  @PostRemove
  void sendMessageOnDelete(Profile resource) {
    super.sendKafkaMessageOnDelete(resource);
  }
}
