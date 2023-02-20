package uk.gov.homeoffice.digital.sas.kafka.listener;

import jakarta.validation.constraints.NotNull;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;

public abstract class KafkaEntityListener<T> {

  private final KafkaProducerService<T> kafkaProducerService;

  protected KafkaEntityListener(KafkaProducerService<T> kafkaProducerService) {
    this.kafkaProducerService = kafkaProducerService;
  }

  public abstract String resolveMessageKey(T resource);

  protected void sendKafkaMessageOnCreate(T resource) {
    sendMessage(resource, KafkaAction.CREATE);
  }

  protected void sendKafkaMessageOnUpdate(T resource) {
    sendMessage(resource, KafkaAction.UPDATE);
  }

  protected void sendKafkaMessageOnDelete(T resource) {
    sendMessage(resource, KafkaAction.DELETE);
  }

  private void sendMessage(@NotNull T resource, KafkaAction action) {
    kafkaProducerService.sendMessage(resolveMessageKey(resource), resource, action);
  }
}
