package uk.gov.homeoffice.digital.sas.kafka.validators;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.regex.Pattern;
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
    List<String> stringList;

    if (schema.contains(",")) {
      stringList = Pattern.compile(", ")
          .splitAsStream(schema).toList();
    } else {
      throw new IllegalArgumentException(String.format("Schema: %s is incorrect ", schema));
    }
    return stringList;
  }

  private boolean isValidMessage(List<String> splitSchema) {
    if (splitSchema.get(0).equals(resourceName)) {
      if (isVersionValid(splitSchema.get(1))) {
        log.info("Schema valid");
        return true;
      }
      return true;
    } else {
      log.error("Entity is not time entry");
      return false;
    }
  }

  private boolean isVersionValid(String value) {
    return validVersions.stream().anyMatch(s -> s.contains(value));
  }


}
