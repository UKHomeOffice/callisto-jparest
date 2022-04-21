package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.*;

import javax.persistence.PersistenceException;
import java.util.logging.Logger;

@ControllerAdvice(assignableTypes = {ResourceApiController.class})
public class ApiResponseExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ApiResponseExceptionHandler.class.getName());

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonProcessingException(JsonProcessingException ex) {
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
        LOGGER.severe("Persistence exception occurred whilst saving a resource. " + ex.getMessage());
        var msg = "There was an error persisting data. Payloads must be valid.";
        return createResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTenantIdException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTenantIdException(InvalidTenantIdException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    
    private ResponseEntity<ApiErrorResponse> createResponseEntity(String message, HttpStatus httpStatus) {

        var apiErrorResponse = new ApiErrorResponse(message);
        return new ResponseEntity<>(apiErrorResponse, httpStatus);
    }


}
