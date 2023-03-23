package uk.gov.homeoffice.digital.sas.kafka.validators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import uk.gov.homeoffice.digital.sas.kafka.enums.TimeEntrySchema;

@Slf4j
public class SchemaValidator {

  public boolean isSchemaValid(String message) throws IOException {
    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();

    String schema = jsonMessage.get("schema").getAsString();

    List<String> splitSchema = splitMessageSchema(schema);

    return isValidMessage(splitSchema);
  }


  private List<String> splitMessageSchema(String schema) {
    List<String> stringList = new ArrayList<>();

    if (schema.contains(",")) {
      stringList = Pattern.compile(", ")
          .splitAsStream(schema)
          .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException(String.format("Schema: %s is incorrect ", schema));
    }
    return stringList;
  }

  private boolean isValidMessage(List<String> splitSchema) {
    if (splitSchema.get(0).contains("uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry")) {
      if (isInEnum(splitSchema.get(1))) {
        log.info("schema valid");
        return true;
      }
    } else {
      log.error("Entity is not time entry");
      return false;
    }
    log.error("Schema is invalid");
    return false;
  }

  private <E extends Enum<E>> boolean isInEnum(String value) {
    for (TimeEntrySchema s : TimeEntrySchema.values()) {
      if (s.getVersion().contains(value)) {
        return true;
      }
    }
    return false;
  }

}
