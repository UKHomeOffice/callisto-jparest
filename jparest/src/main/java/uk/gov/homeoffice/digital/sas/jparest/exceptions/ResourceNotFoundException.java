package uk.gov.homeoffice.digital.sas.jparest.exceptions;

public class ResourceNotFoundException extends RuntimeException {

  private static final String RESOURCE_NOT_FOUND_ERROR_FORMAT =
      "Resource with id: %s was not found";


  public ResourceNotFoundException() {
    super();
  }

  public ResourceNotFoundException(Object id) {
    super(String.format(RESOURCE_NOT_FOUND_ERROR_FORMAT, id));
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }

}
