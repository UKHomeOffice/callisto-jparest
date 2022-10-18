package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>A class to hold information relating to an error thrown by the API.</p>
 */
@Getter
@AllArgsConstructor
public class ApiErrorResponse {

  private final String message;

}