package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SUCCESSFUL_DESERIALIZATION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.RESOURCE_JSON_ATTRIBUTE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_JSON_ATTRIBUTE;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import uk.gov.homeoffice.digital.sas.kafka.message.Messageable;

@Slf4j
@Component
public class KafkaConsumerUtils<T extends Messageable> {

  public void checkDeserializedResource(String payload, T resource) {
    if (ObjectUtils.isEmpty(resource)) {
      log.error(String.format(KAFKA_DESERIALIZATION_TO_CONCRETE_TYPE_FAILED, payload));
    } else {
      log.info(String.format(KAFKA_SUCCESSFUL_DESERIALIZATION, payload));
    }
  }

  public static String getSchemaFromMessageAsString(String payload) {
    JsonObject jsonMessage = JsonParser.parseString(payload).getAsJsonObject();
    return jsonMessage.get(SCHEMA_JSON_ATTRIBUTE).getAsString();
  }

  public static String getResourceFromMessageAsString(String payload) {
    JsonObject jsonMessage = JsonParser.parseString(payload).getAsJsonObject();
    return jsonMessage.get(RESOURCE_JSON_ATTRIBUTE).toString();
  }
}
