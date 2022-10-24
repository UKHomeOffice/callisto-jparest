package uk.gov.homeoffice.digital.sas.jparest.exceptions;


public class ResourceException extends RuntimeException {
  /**
   * Constructs an {@code ResourceException} with no
   * detail message.
   */
  public ResourceException() {
    super();
  }

  /**
   * Constructs an {@code ResourceException} with the
   * specified detail message.
   *
   * @param s the detail message.
   */
  public ResourceException(String s) {
    super(s);
  }

}
