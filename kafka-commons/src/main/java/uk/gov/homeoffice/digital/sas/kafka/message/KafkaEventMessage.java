package uk.gov.homeoffice.digital.sas.kafka.message;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class KafkaEventMessage<T> {

  public static final String SCHEMA_FORMAT = "%s, %s";

  private String schema;

  @NotNull
  private T resource;

  @NotNull
  private KafkaAction action;

  public KafkaEventMessage(String projectVersion, Class<T> resourceType, T resource,
      KafkaAction action) {
    this.schema = String.format(SCHEMA_FORMAT, resourceType.getName(), projectVersion);
    this.resource = resource;
    this.action = action;
  }
}
