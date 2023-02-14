package uk.gov.homeoffice.digital.sas.kafka.message;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class KafkaEventMessage<T> {

  private final String schema;

  @NotNull
  private final T resource;

  @NotNull
  private final KafkaAction action;

  public KafkaEventMessage(String projectVersion, Class<T> resourceType, T resource,
      KafkaAction action) {
    this.schema = resourceType.getName() + ", " + projectVersion;
    this.resource = resource;
    this.action = action;
  }
}
