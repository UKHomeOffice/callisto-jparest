package uk.gov.homeoffice.digital.sas.kafka.validators;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public abstract class SchemaValidator {

  String resourceName;

  DefaultArtifactVersion supportedVersion;

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
    DefaultArtifactVersion messageVersion = new DefaultArtifactVersion(version);

    switch (isMajorVersionValid(messageVersion.getMajorVersion())) {
      case 1:
        log.error(String.format(KAFKA_SCHEMA_INVALID_VERSION, version));
        return false;
      case -1:
        log.info(String.format(KAFKA_SCHEMA_VALIDATED, version));
        return true;
      case 0:
        if (messageVersion.getMinorVersion() > supportedVersion.getMinorVersion()) {
          log.error(String.format(KAFKA_SCHEMA_INVALID_VERSION, version));
          return false;
        } else {
          log.info(String.format(KAFKA_SCHEMA_VALIDATED, version));
          return true;
        }
      default:
        return false;
    }
  }

  private int isMajorVersionValid(int majorVersion) {
    return Integer.compare(majorVersion, supportedVersion.getMajorVersion());
  }
}
