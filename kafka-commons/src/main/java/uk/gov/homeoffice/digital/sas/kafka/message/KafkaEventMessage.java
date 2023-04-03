package uk.gov.homeoffice.digital.sas.kafka.message;

import static uk.gov.homeoffice.digital.sas.kafka.constants.Constants.SCHEMA_FORMAT;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KafkaEventMessage<T> {

  private final String schema;

  @NotNull
  private final T resource;

  @NotNull
  private final KafkaAction action;

  public KafkaEventMessage(String schemaVersion, T resource,
      KafkaAction action) {
    this.schema = String.format(SCHEMA_FORMAT, resource.getClass().getName(), schemaVersion);
    this.resource = resource;
    this.action = action;
  }
}
