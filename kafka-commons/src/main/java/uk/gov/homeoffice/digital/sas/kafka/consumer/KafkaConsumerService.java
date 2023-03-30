package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
@Service
@Getter
public class KafkaConsumerService<T> {

  private final SchemaValidator schemaValidator;

  protected KafkaConsumerService(SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  /**
   * If payload is invalid consume returns null.
   * The null value is up to the service to handle
   */
  public KafkaEventMessage<T> consume(String payload
  ) throws JsonProcessingException {

    if (schemaValidator.isSchemaValid(payload)) {
      log.info(String.format(KAFKA_CONSUMING_MESSAGE, payload));

      return new Gson().fromJson(payload, new TypeToken<KafkaEventMessage<T>>() { }.getType());
    }

    return null;
  }
}
