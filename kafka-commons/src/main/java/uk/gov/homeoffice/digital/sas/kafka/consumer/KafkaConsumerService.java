package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_CONSUMING_MESSAGE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
@Service
@Getter
public class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;

  private ObjectMapper mapper = new ObjectMapper();

  private final SchemaValidator schemaValidator;

  @Value("${kafka.resource.name}")
  String expectedResource;

  protected KafkaConsumerService(SchemaValidator schemaValidator) {
    this.schemaValidator = schemaValidator;
  }

  public KafkaEventMessage<T> consume(String payload
  ) throws JsonProcessingException {

    if (schemaValidator.isSchemaValid(payload)) {
      log.info(String.format(KAFKA_CONSUMING_MESSAGE, payload));

      schemaValidator.setExpectedResource(expectedResource);
      reactToDifferentResource(schemaValidator.compareResources());

      return mapper.readValue(payload, new TypeReference<>() {});
    }

    return null;
  }

  public void setExpectedResource(String expectedResource) {
    this.expectedResource = expectedResource;
  }

  // will obe overridden in concrete service classes
  public void reactToDifferentResource(boolean s) {
    System.out.println(String.format("Resource is : { %s } I am doing something ...", s));
  }

}
