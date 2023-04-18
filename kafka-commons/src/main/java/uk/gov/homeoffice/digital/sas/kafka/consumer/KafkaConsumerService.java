package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_PAYLOAD_IS_NULL;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESSFUL_DESERIALIZATION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.RESOURCE_TYPE_IS_NULL;
import static uk.gov.homeoffice.digital.sas.kafka.consumer.KafkaConsumerUtils.getSchemaFromMessageAsString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.homeoffice.digital.sas.kafka.consumer.validators.SchemaValidator;
import uk.gov.homeoffice.digital.sas.kafka.exceptions.KafkaConsumerException;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;

@Slf4j
@Service
@Getter
public class KafkaConsumerService<T> {

  public static final ObjectMapper objectMapper = new ObjectMapper();

  private final SchemaValidator schemaValidator;

  public KafkaConsumerService(SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  public KafkaEventMessage<T> convertToKafkaEventMessage(String payload
  ) throws JsonProcessingException {
    if (schemaValidator.isSchemaValid(payload)) {
      log.info(String.format(KAFKA_CONSUMING_MESSAGE, payload));
      return objectMapper.readValue(payload, new TypeReference<>() {});
    } else {
      throw new KafkaConsumerException(String.format(KAFKA_SCHEMA_INVALID_VERSION,
          getSchemaFromMessageAsString(payload)));
    }
  }

  public boolean isResourceOfType(@NotNull String payload, @NotNull Class<T> type) {
    if (payload == null) {
      throw new KafkaConsumerException(KAFKA_PAYLOAD_IS_NULL);
    }
    if (type == null) {
      throw new KafkaConsumerException(RESOURCE_TYPE_IS_NULL);
    }

    JsonParser.parseString(payload).getAsJsonObject();
    String schema = getSchemaFromMessageAsString(payload);
    return schema.contains(type.getSimpleName());
  }
}
