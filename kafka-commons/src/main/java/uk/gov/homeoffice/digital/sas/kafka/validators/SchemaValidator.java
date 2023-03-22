package uk.gov.homeoffice.digital.sas.kafka.validators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class SchemaValidator {

  public boolean validateSchema(String message, String file) throws IOException {
    FileReader fileReader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    List<String> lines = new ArrayList<String>();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      lines.add(line);
    }
    bufferedReader.close();

    return isSchemaValid(lines, message);
  }

  private boolean isSchemaValid(List<String> validSchemas, String message) {
    JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();

    JsonElement schema = jsonMessage.get("schema");

    List<String> splitSchema = splitSchema(String.valueOf(schema));

    if (splitSchema.get(0).contains("uk.gov.homeoffice.digital.sas.timecard.model.TimeEntry")) {
      if (validSchemas.contains(splitSchema.get(1))) {
        log.info("schema valid");
        return true;
      }
    } else {
      log.error("Entity is not time entry");
      return false;
    }
    return false;
  }

  private List<String> splitSchema(String schema) {
    List<String> stringList = new ArrayList<>();

    if (schema.contains(",")) {
      stringList = Pattern.compile(", ")
          .splitAsStream(schema)
          .map(String::trim)
          .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException(String.format("Schema: %s is incorrect ", schema));
    }
    return stringList;
  }
}
