package uk.gov.homeoffice.digital.sas.kafka.message;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KafkaEventMessage<T> {

  public static final String SCHEMA_FORMAT = "%s, %s";

  private final String schema;

  @NotNull
  private final T resource;

  @NotNull
  private final KafkaAction action;

  public KafkaEventMessage(String projectVersion, T resource,
      KafkaAction action) {
    this.schema = String.format(SCHEMA_FORMAT, resource.getClass().getName(), projectVersion);
    this.resource = resource;
    this.action = action;
  }
}
