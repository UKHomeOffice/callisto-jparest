package uk.gov.homeoffice.digital.sas.kafka.validators;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.*;

@Slf4j
@Component
@Getter
@Setter
@NoArgsConstructor
public class SchemaValidator {

  String resourceName;

  List<String> validVersions;

  public SchemaValidator(String resourceName,
                         List<String> validVersions) {
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
    if(splitSchema.size() != 2) return  false;
    if (isValidResource(splitSchema.get(0))
        && isVersionValid(splitSchema.get(1)) ) {
      log.info(String.format(KAFKA_SCHEMA_VALIDATED,
          splitSchema.stream().collect(Collectors.joining(", "))));
      return true;
    } else {
      log.error(String.format(KAFKA_SCHEMA_INVALID,
        splitSchema.stream().collect(Collectors.joining(", "))));
      return false;
    }
  }

  private boolean isValidResource(String resource) {
    if(!resource.equals(resourceName)) {
      log.error(String.format(KAFKA_SCHEMA_INVALID_RESOURCE,resource));
      return false;
    }
    return true;
  }

  private boolean isVersionValid(String version) {
     if(validVersions.stream().noneMatch(s -> s.contains(version))) {
       log.error(String.format(KAFKA_SCHEMA_INVALID_VERSION, version));
       return false;
     }
     return true;
  }

}
