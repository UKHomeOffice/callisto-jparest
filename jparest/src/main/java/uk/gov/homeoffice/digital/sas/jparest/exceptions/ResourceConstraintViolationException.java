package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import java.util.Arrays;

public class ResourceConstraintViolationException extends RuntimeException {

  public ResourceConstraintViolationException() {
    super();
  }

  public ResourceConstraintViolationException(String s) {
    super(s);
  }

  public ResourceConstraintViolationException(Object[] o) {
    super(
            Arrays.toString(o));
  }

}
