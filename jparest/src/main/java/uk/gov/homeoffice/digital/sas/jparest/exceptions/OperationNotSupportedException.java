package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class OperationNotSupportedException extends IllegalArgumentException {

  public OperationNotSupportedException(String operation) {
    super("The supplied operation is not supported: " + operation);
  }
}
