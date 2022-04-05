package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import lombok.*;


/**
 * A class to hold information relating to an error thrown by the API
 */
@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private final String httpStatus;
    private final String message;
    private final String time;

}