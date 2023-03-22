package uk.gov.homeoffice.digital.sas.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.homeoffice.digital.sas.kafka.message.KafkaEventMessage;

@Slf4j
public abstract class KafkaConsumerService<T> {

  protected KafkaEventMessage<T> kafkaEventMessage;
  ObjectMapper mapper = new ObjectMapper();


  @Value("${time.entry.current.version}")
  String timeEntryCurrentVersion;

  @KafkaListener(
      topics = "${spring.kafka.template.default-topic}",
      groupId = "${spring.kafka.consumer.group-id}")

  public void consumer(@Payload String message
  ) throws JsonProcessingException {

    schemaValidation(message);

  }

  private boolean isSchemaValid(String schema) {
    String fullSchema = "uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry, "
        + timeEntryCurrentVersion;
    String snapShotSchema = fullSchema + "-SNAPSHOT";

    if (schema.equals(fullSchema)) {
      return true;
    } else if (schema.equals(snapShotSchema)) {
      return true;
    }
    return false;

  }

  private void schemaValidation(String message) throws JsonProcessingException {

    JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
    String jsonSchema = jsonObject.get("schema").getAsString();

    if (isSchemaValid(jsonSchema)) {
      log.info(String.format("Schema valid: %s", jsonSchema));
      log.info("consuming message =" + message);
      KafkaEventMessage kafkaEventMessage = mapper.readValue(message, KafkaEventMessage.class);
    } else {
      log.error("Schema invalid");
    }
  }

}
