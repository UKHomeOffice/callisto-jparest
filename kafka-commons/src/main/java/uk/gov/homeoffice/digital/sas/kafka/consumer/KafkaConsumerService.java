package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_FAILED_DESERIALIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
@Service
@Getter
public abstract class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;

  ObjectMapper mapper = new ObjectMapper();

  String resourceName;

  List<String> validVersions;

  String payload;

  protected KafkaConsumerService(@Value("${kafka.resource.name}") String resourceName,
                                 @Value("${kafka.valid.schema.versions}") List<String> validVersions) {
    this.resourceName = resourceName;
    this.validVersions = validVersions;
  }

  @KafkaListener(
      topics = "${spring.kafka.template.default-topic}",
      groupId = "${spring.kafka.consumer.group-id}")

  public void consumer(@Payload String message
  ) {
    this.payload = message;
    SchemaValidator schemaValidator = new SchemaValidator(resourceName, validVersions);
    if (schemaValidator.isSchemaValid(message)) {
      try {
        log.info(String.format(KAFKA_CONSUMING_MESSAGE, message));
        kafkaEventMessage = mapper.readValue(message, KafkaEventMessage.class);
      } catch (JsonProcessingException e) {
        log.error(KAFKA_FAILED_DESERIALIZATION, e);
      }
    }
  }

  public String getPayload() {
    return this.payload;
  }
}
