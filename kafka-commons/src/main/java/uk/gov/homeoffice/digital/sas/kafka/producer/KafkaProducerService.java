package uk.gov.homeoffice.digital.sas.kafka.producer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_FAILED_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESS_MESSAGE;

import jakarta.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaAction;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;

@Service
@EnableAutoConfiguration
public class KafkaProducerService<T> {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

  private final KafkaTemplate<String, KafkaEventMessage<T>> kafkaTemplate;
  private final String topicName;
  private final String schemaVersion;

  public KafkaProducerService(
      KafkaTemplate<String, KafkaEventMessage<T>> kafkaTemplate,
      @Value("${spring.kafka.template.default-topic}") String topicName,
      @Value("${schemaVersion}") String schemaVersion) {
    this.kafkaTemplate = kafkaTemplate;
    this.topicName = topicName;
    this.schemaVersion = schemaVersion;
  }

  public void sendMessage(@NotNull String messageKey, @NotNull T resource, KafkaAction action) {
    var kafkaEventMessage =
        new KafkaEventMessage<>(schemaVersion, resource, action);
    CompletableFuture<SendResult<String, KafkaEventMessage<T>>> future = null;
    try {
      future = kafkaTemplate.send(
          topicName,
          messageKey,
          kafkaEventMessage
      );
      completeKafkaTransaction(future);
      logKafkaMessage(messageKey, kafkaEventMessage, future);
    } catch (ExecutionException | InterruptedException e) {
      if  (Thread.interrupted()) {
        Thread.currentThread().interrupt();
      }
      log.error(String.format(
          KAFKA_FAILED_MESSAGE,
          messageKey, topicName, kafkaEventMessage.getAction()), e);
    }
  }

  private void logKafkaMessage(
      String messageKey,
      KafkaEventMessage<T> kafkaEventMessage,
      CompletableFuture<SendResult<String,
          KafkaEventMessage<T>>> future) {
    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info(String.format(KAFKA_SUCCESS_MESSAGE,
            messageKey, topicName, kafkaEventMessage.getAction()));
      }
    });
  }

  private void completeKafkaTransaction(
      CompletableFuture<SendResult<String, KafkaEventMessage<T>>> future)
      throws ExecutionException, InterruptedException {
    future.complete(future.get());
  }
}
