package uk.gov.homeoffice.digital.sas.kafka.listener;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.validation.constraints.NotNull;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;

public final class KafkaEntityListener<T extends Messageable> {

  private final KafkaProducerService<T> kafkaProducerService;

  public KafkaEntityListener(KafkaProducerService<T> kafkaProducerService) {
    this.kafkaProducerService = kafkaProducerService;
  }

  @PostPersist
  private void sendKafkaMessageOnCreate(T resource) {
    sendMessage(resource, KafkaAction.CREATE);
  }

  @PostUpdate
  private void sendKafkaMessageOnUpdate(T resource) {
    sendMessage(resource, KafkaAction.UPDATE);
  }

  @PostRemove
  private void sendKafkaMessageOnDelete(T resource) {
    sendMessage(resource, KafkaAction.DELETE);
  }

  private void sendMessage(@NotNull T resource, KafkaAction action) {
    kafkaProducerService.sendMessage(resource.resolveMessageKey(), resource, action);
  }
}
