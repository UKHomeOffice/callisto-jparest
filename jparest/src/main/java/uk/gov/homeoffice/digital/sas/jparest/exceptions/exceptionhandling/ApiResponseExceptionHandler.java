package uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.InvalidFilterException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnexpectedQueryResultException;
import uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper;

import javax.persistence.PersistenceException;
import java.util.logging.Logger;

import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.SERVER_ERROR;

@ControllerAdvice(assignableTypes = {ResourceApiController.class})
public class ApiResponseExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ApiResponseExceptionHandler.class.getName());

    @ExceptionHandler({
            IllegalArgumentException.class,
            JsonProcessingException.class,
            InvalidFilterException.class,
            ResourceConstraintViolationException.class,
            UnknownResourcePropertyException.class,
            TenantIdMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(TypeMismatchException ex) {
        var msg = "Parameters must be of the relevant types specified by the API";
        return createResponseEntity(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiErrorResponse> handlePersistenceException(PersistenceException ex) {
        LOGGER.severe(SERVER_ERROR + ex.getMessage());
        return createResponseEntity(SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnexpectedQueryResultException.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedQueryResultException(UnexpectedQueryResultException ex) {
        return createResponseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<ApiErrorResponse> createResponseEntity(String message, HttpStatus httpStatus) {
        var apiErrorResponse = new ApiErrorResponse(message);
        return new ResponseEntity<>(apiErrorResponse, httpStatus);
    }

}