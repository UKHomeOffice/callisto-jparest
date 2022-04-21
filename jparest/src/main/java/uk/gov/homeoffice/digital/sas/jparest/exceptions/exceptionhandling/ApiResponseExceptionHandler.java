package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

@ControllerAdvice(assignableTypes = {ResourceApiController.class})
public class ApiResponseExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonProcessingException(JsonProcessingException ex) {
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFilterException(InvalidFilterException ex) {
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    private ResponseEntity<ApiErrorResponse> createResponseEntity(Exception exception, HttpStatus httpStatus) {

        var apiErrorResponse = new ApiErrorResponse(exception.getMessage());

        return new ResponseEntity<>(apiErrorResponse, httpStatus);
    }


}
