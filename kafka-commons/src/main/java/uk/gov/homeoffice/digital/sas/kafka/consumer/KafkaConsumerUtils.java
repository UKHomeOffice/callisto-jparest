package uk.gov.homeoffice.digital.sas.kafka.consumer;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.RESOURCE_JSON_ATTRIBUTE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_JSON_ATTRIBUTE;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumerUtils {

  private KafkaConsumerUtils() {
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
