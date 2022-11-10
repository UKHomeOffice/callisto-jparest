package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import lombok.Getter;

@Getter
public class StructuredError {

  private final String field;
  private final String message;
  private final Object data;

  public StructuredError(String field, String message, Object data) {
    this.field = field;
    this.message = message;
    this.data = data;
  }
}
