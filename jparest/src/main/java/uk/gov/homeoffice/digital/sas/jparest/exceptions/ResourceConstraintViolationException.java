package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.List;
import lombok.Getter;

public class ResourceConstraintViolationException extends RuntimeException {

  @Getter
  private final List<StructuredError> errorResponse;

  public ResourceConstraintViolationException(List<StructuredError> o) {
    this.errorResponse = o;
  }

}
