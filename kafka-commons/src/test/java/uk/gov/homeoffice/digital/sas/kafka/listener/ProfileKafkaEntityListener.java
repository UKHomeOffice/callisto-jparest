package uk.gov.homeoffice.digital.sas.kafka.listener;

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
}
