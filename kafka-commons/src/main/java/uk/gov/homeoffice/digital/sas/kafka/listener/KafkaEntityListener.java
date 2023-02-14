package uk.gov.homeoffice.digital.sas.kafka.listener;

import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;

public abstract class KafkaEntityListener<T> {

  protected KafkaProducerService<T> kafkaProducerService;

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

  @SuppressWarnings("unchecked")
  private void sendMessage(T resource, KafkaAction action) {
    kafkaProducerService.sendMessage(resolveMessageKey(resource),
        (Class<T>) resource.getClass(), resource, action);
  }
}
