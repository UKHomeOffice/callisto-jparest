package uk.gov.homeoffice.digital.sas.jparest.exceptions;

/**
 * Thrown when a SpelExpression can't be converted to a
 * predicate.
 */
public class InvalidFilterException extends RuntimeException {
  /**
   * Constructs an {@code InvalidFilterException} with no
   * detail message.
   */
  public InvalidFilterException() {
    super();
  }

  /**
   * Constructs an {@code InvalidFilterException} with the
   * specified detail message.
   *
   * @param s the detail message.
   */
  public InvalidFilterException(String s) {
    super(s);
  }

}
