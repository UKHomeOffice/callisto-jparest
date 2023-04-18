package uk.gov.homeoffice.digital.sas.kafka.message;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_FORMAT;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KafkaEventMessage<T> {

  @JsonProperty("schema")
  private String schema;

  @NotNull
  @JsonProperty("resource")
  private T resource;

  @NotNull
  @JsonProperty("action")
  private KafkaAction action;

  public KafkaEventMessage(String schemaVersion, T resource,
                           KafkaAction action) {
    this.schema = String.format(SCHEMA_FORMAT, resource.getClass().getName(), schemaVersion);
    this.resource = resource;
    this.action = action;
  }

  public KafkaEventMessage() {}
}
