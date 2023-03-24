package uk.gov.homeoffice.digital.sas.kafka.validators;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INCORRECT_FORMAT;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaValidator {
  String resourceName;

  List<String> validVersions;

  public SchemaValidator(String resourceName, List<String> validVersions) {
    this.resourceName = resourceName;
    this.validVersions = validVersions;
  }

  public boolean isSchemaValid(String message) {
    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
    String schema = jsonMessage.get(SCHEMA).getAsString();
    List<String> splitSchema = splitMessageSchema(schema);

    return isValidMessage(splitSchema);
  }

  private List<String> splitMessageSchema(String schema) {
    List<String> stringList = new ArrayList<>();
    try {
      if (schema.contains(",")) {
        stringList = Pattern.compile(", ")
            .splitAsStream(schema).toList();
      }
    } catch (IllegalArgumentException e) {
      log.error(String.format(KAFKA_SCHEMA_INCORRECT_FORMAT,
          schema), e);
    }
    return stringList;
  }

  private boolean isValidMessage(List<String> splitSchema) {
    if (splitSchema.stream().anyMatch(this::isValidResource)
        && splitSchema.stream().anyMatch(this::isVersionValid)) {
      log.info(String.format(KAFKA_SCHEMA_VALIDATED,
          splitSchema.stream().collect(Collectors.joining(", "))));
      return true;
    } else {
      log.error(KAFKA_SCHEMA_INVALID,
          splitSchema.stream().collect(Collectors.joining(", ")));
      return false;
    }
  }

  private boolean isValidResource(String value) {
    return value.equals(resourceName);
  }

  private boolean isVersionValid(String value) {
    return validVersions.stream().anyMatch(s -> s.contains(value));
  }


}
