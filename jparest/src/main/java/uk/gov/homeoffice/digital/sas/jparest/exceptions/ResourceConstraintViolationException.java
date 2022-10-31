package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import lombok.Getter;

public class ResourceConstraintViolationException extends RuntimeException {

  @Getter
  private Object[] errorResponse = {};

  public ResourceConstraintViolationException(String s) {
    super(s);
  }

  public ResourceConstraintViolationException(Object[] o) {
    this.errorResponse = o;
  }

}
