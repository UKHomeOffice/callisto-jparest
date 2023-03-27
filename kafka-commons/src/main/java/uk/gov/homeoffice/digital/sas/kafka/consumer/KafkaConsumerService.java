package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_FAILED_DESERIALIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
@Service
@Getter
public abstract class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;

  private ObjectMapper mapper = new ObjectMapper();

  @Value("${kafka.resource.name}")
  private String resourceName;

  @Value("${kafka.valid.schema.versions}")
  private List<String> validVersions;

  private final SchemaValidator schemaValidator;

  protected KafkaConsumerService(SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  public KafkaEventMessage<T> consumer(String payload
  ) {
    schemaValidator.setValidVersions(validVersions);
    schemaValidator.setResourceName(resourceName);
    if (schemaValidator.isSchemaValid(payload)) {
      try {
        log.info(String.format(KAFKA_CONSUMING_MESSAGE, payload));
        return mapper.readValue(payload, KafkaEventMessage.class);
      } catch (JsonProcessingException e) {
        log.error(KAFKA_FAILED_DESERIALIZATION, e);
      }
    }
    return null;
  }
}