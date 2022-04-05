package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;

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
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(IOException ex) {
        return createResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<ApiErrorResponse> createResponseEntity(Exception exception, HttpStatus httpStatus) {

        var apiErrorResponse = new ApiErrorResponse(
                String.valueOf(httpStatus.value()),
                exception.getMessage(),
                Date.from(clock.instant()).toString());

        return new ResponseEntity<>(apiErrorResponse, httpStatus);
    }


}
