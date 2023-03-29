package uk.gov.homeoffice.digital.sas.kafka.validators;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INCORRECT_FORMAT;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_RESOURCE;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.versioning.ComparableVersion;


@Slf4j
@Getter
@Setter
@NoArgsConstructor
public abstract class SchemaValidator {

  String resourceName;

  ComparableVersion validVersion;

  public boolean isSchemaValid(String message) {
    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
    String schema = jsonMessage.get(SCHEMA).getAsString();
    List<String> splitSchema = splitMessageSchema(schema);
    return isValidMessage(splitSchema, schema);
  }

  private List<String> splitMessageSchema(String schema) {
    List<String> stringList = new ArrayList<>();
    if (schema.contains(",")) {
      stringList = Pattern.compile(", ")
          .splitAsStream(schema).toList();
    }
    return stringList;
  }

  private boolean isValidMessage(List<String> splitSchema, String schema) {
    if (splitSchema.size() == 2
        && isValidResource(splitSchema.get(0))
        && isVersionValid(splitSchema.get(1))) {
      log.info(String.format(KAFKA_SCHEMA_VALIDATED, schema));
      return true;
    } else {
      log.error(String.format(KAFKA_SCHEMA_INVALID, schema));
      return false;
    }
  }

  private boolean isValidResource(String resource) {
    if (!resource.equals(resourceName)) {
      log.error(String.format(KAFKA_SCHEMA_INVALID_RESOURCE, resource));
      return false;
    }
    return true;
  }

  private boolean isVersionValid(String version) {
    ComparableVersion messageVersion = new ComparableVersion(version);
   if (validVersion.compareTo(messageVersion) < 0) {
     log.error(String.format(KAFKA_SCHEMA_INVALID_VERSION, version));
     return false;
    }
    return true;
  }
}
