package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
@Service
@Getter
public class KafkaConsumerService<T> {

  public static final ObjectMapper objectMapper = new ObjectMapper();

  private final SchemaValidator schemaValidator;

  protected KafkaConsumerService(SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  public KafkaEventMessage<T> convertToKafkaEventMessage(String payload
  ) throws JsonProcessingException {
    if (schemaValidator.isSchemaValid(payload)) {
      log.info(String.format(KAFKA_CONSUMING_MESSAGE, payload));
      return objectMapper.readValue(payload, new TypeReference<>() {});
    } else {
      throw new KafkaConsumerException(String.format(KAFKA_SCHEMA_INVALID_VERSION, payload));
    }
  }
}
