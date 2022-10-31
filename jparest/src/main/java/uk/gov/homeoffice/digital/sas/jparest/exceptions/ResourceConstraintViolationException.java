package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import lombok.Getter;

public class ResourceConstraintViolationException extends RuntimeException {

  @Getter
  private final Object[] errorResponse;

  public ResourceConstraintViolationException(Object[] o) {
    this.errorResponse = o;
  }

}
