package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A class to hold information relating to an error thrown by the API
 */
@Getter
@AllArgsConstructor
public class ApiErrorResponse {

  private final String message;

}