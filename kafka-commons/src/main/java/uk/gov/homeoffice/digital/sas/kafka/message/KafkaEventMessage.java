package uk.gov.homeoffice.digital.sas.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class KafkaEventMessage<T> {

  public static final String SCHEMA_FORMAT = "%s, %s";

  @JsonProperty("schema")
  private String schema;

  @NotNull
  @JsonProperty("resource")
  private T resource;

  @NotNull
  @JsonProperty("action")
  private KafkaAction action;

  public KafkaEventMessage(String projectVersion, T resource,
      KafkaAction action) {
    this.schema = String.format(SCHEMA_FORMAT, resource.getClass().getName(), projectVersion);
    this.resource = resource;
    this.action = action;
  }

  public KafkaEventMessage() {}
}
