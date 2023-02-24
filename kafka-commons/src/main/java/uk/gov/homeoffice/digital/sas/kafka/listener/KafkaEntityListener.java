package uk.gov.homeoffice.digital.sas.kafka.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.validation.constraints.NotNull;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;

public class KafkaEntityListener<T extends Messageable> {

  private final KafkaProducerService<T> kafkaProducerService;

  protected KafkaEntityListener(KafkaProducerService<T> kafkaProducerService) {
    this.kafkaProducerService = kafkaProducerService;
  }

  @PostPersist
  protected void sendKafkaMessageOnCreate(T resource) {
    sendMessage(resource, KafkaAction.CREATE);
  }

  @PostUpdate
  protected void sendKafkaMessageOnUpdate(T resource) {
    sendMessage(resource, KafkaAction.UPDATE);
  }

  @PostRemove
  protected void sendKafkaMessageOnDelete(T resource) {
    sendMessage(resource, KafkaAction.DELETE);
  }

  private void sendMessage(@NotNull T resource, KafkaAction action) {
    kafkaProducerService.sendMessage(resource.resolveMessageKey(), resource, action);
  }
}
