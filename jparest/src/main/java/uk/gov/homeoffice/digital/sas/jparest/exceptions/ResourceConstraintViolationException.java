package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.ArrayList;
import lombok.Getter;

public class ResourceConstraintViolationException extends RuntimeException {

  @Getter
  private final ArrayList<StructuredError> errorResponse;

  public ResourceConstraintViolationException(ArrayList<StructuredError> o) {
    this.errorResponse = o;
  }

}
