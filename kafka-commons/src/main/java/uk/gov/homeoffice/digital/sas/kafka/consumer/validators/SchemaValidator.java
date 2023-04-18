package uk.gov.homeoffice.digital.sas.kafka.consumer.validators;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INCORRECT_FORMAT;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_INVALID_VERSION;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.KAFKA_SCHEMA_VALIDATED;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_COMMA_DELIMETER;
import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_JSON_ATTRIBUTE;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.semver4j.Semver;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaValidator {

  private String supportedVersion;

  public SchemaValidator(@Value("${kafka.supported.schema.version}") String supportedVersion) {
    this.supportedVersion = supportedVersion;
  }

  public boolean isSchemaValid(String message) {
    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
    String schema = jsonMessage.get(SCHEMA_JSON_ATTRIBUTE).getAsString();
    List<String> splitSchema = splitMessageSchema(schema);
    return isValidMessage(splitSchema, schema);
  }

  private List<String> splitMessageSchema(String schema) {
    List<String> stringList = new ArrayList<>();
    if (schema.contains(SCHEMA_COMMA_DELIMETER)) {
      stringList = Pattern.compile(SCHEMA_COMMA_DELIMETER)
          .splitAsStream(schema).toList();
    }
    return stringList;
  }

  private boolean isValidMessage(List<String> splitSchema, String schema) {
    if (splitSchema.size() == 2) {
      if (isVersionValid(splitSchema.get(1))) {
        log.info(String.format(KAFKA_SCHEMA_VALIDATED, schema));
        return true;
      }
    } else {
      log.error(String.format(KAFKA_SCHEMA_INCORRECT_FORMAT, schema));
      return false;
    }
    return false;
  }

  private boolean isVersionValid(String version) {
    var semver = new Semver(version, Semver.SemverType.NPM);
    if (semver.withClearedSuffix().satisfies(supportedVersion)) {
      return true;
    } else {
      log.error((String.format(KAFKA_SCHEMA_INVALID_VERSION, version)));
      return false;
    }
  }
}
