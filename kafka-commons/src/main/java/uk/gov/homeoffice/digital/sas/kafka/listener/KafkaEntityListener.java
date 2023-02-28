package uk.gov.homeoffice.digital.sas.kafka.listener;

import java.util.function.BiConsumer;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;
import uk.gov.homeoffice.digital.sas.kafka.producer.KafkaProducerService;
import uk.gov.homeoffice.digital.sas.kafka.transactionsync.KafkaDbTransactionSynchronizer;

public final class KafkaEntityListener<T extends Messageable> {

  private final KafkaProducerService<T> kafkaProducerService;

  @Autowired
  private KafkaDbTransactionSynchronizer kafkaDbTransactionSynchronizer;

  public KafkaEntityListener(KafkaProducerService<T> kafkaProducerService) {
    this.kafkaProducerService = kafkaProducerService;
  }

  @PrePersist
  private void sendKafkaMessageOnCreate(T resource) {
    sendMessage(resource, KafkaAction.CREATE);
  }

  @PreUpdate
  private void sendKafkaMessageOnUpdate(T resource) {
    sendMessage(resource, KafkaAction.UPDATE);
  }

  @PreRemove
  private void sendKafkaMessageOnDelete(T resource) {
    sendMessage(resource, KafkaAction.DELETE);
  }

  private void sendMessage(@NotNull T resource, KafkaAction action) {
    BiConsumer<KafkaAction, String> sendMessageConsumer =
        (KafkaAction actionArg, String messageKeyArg) -> kafkaProducerService.sendMessage(
            messageKeyArg, resource, actionArg);

    kafkaDbTransactionSynchronizer.registerSynchronization(
        action, resource.resolveMessageKey(), sendMessageConsumer);

  }
}
