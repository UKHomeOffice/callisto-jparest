package uk.gov.homeoffice.digital.sas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;
import uk.gov.homeoffice.digital.sas.kafka.validators.SchemaValidator;

@Slf4j
public abstract class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;

  @Value("${time.entry.schema.file.location}")
  private String file;

  private SchemaValidator schemaValidator = new SchemaValidator();
  ObjectMapper mapper = new ObjectMapper();


  @KafkaListener(
      topics = "${spring.kafka.template.default-topic}",
      groupId = "${spring.kafka.consumer.group-id}")

  public void consumer(@Payload String message
  ) throws IOException {
    if (schemaValidator.isSchemaValid(message)) {
      log.info("consuming message =" + message);
      KafkaEventMessage kafkaEventMessage = mapper.readValue(message, KafkaEventMessage.class);
    }
  }
}
