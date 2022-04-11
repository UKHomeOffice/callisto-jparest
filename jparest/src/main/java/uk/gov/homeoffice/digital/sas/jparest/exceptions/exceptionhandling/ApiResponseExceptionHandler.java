package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.time.Clock;
import java.util.Date;

@ControllerAdvice
public class ApiResponseExceptionHandler {


    private final Clock clock;

    @Autowired
    public ApiResponseExceptionHandler(Clock clock) {
        this.clock = clock;
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(IOException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFilterException(InvalidFilterException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConstraintViolationException(ResourceConstraintViolationException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnknownResourcePropertyException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownResourcePropertyException(UnknownResourcePropertyException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiErrorResponse> handlePersistenceException(PersistenceException ex) {
        var msg = "There was an error persisting data. Payloads must be valid.";
        return createResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<ApiErrorResponse> createResponseEntity(String message, HttpStatus httpStatus) {

        var apiErrorResponse = new ApiErrorResponse(
                String.valueOf(httpStatus.value()),
                message,
                Date.from(clock.instant()).toString());

        return new ResponseEntity<>(apiErrorResponse, httpStatus);
    }


}
