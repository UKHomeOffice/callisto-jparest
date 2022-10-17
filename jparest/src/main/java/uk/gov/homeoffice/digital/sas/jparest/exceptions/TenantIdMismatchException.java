package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class TenantIdMismatchException extends IllegalArgumentException {

  public TenantIdMismatchException() {
    super("The supplied payload tenant id value "
        + "must match the url tenant id query parameter value");
  }
}
