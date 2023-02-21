package uk.gov.homeoffice.digital.sas.kafka.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum KafkaAction {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete");
  private final String stringValue;

  @Override
  public String toString() {
    return stringValue;
  }
}