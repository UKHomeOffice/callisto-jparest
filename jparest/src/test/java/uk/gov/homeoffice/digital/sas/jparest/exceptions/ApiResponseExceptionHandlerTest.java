package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiErrorResponse;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;

import javax.persistence.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


class ApiResponseExceptionHandlerTest {

    private static final String ERROR_MESSAGE = "error message";

    @Test
    void handleIllegalArgumentException_badRequestWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new IllegalArgumentException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleIllegalArgumentException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceNotFoundException_notFoundWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new ResourceNotFoundException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleResourceNotFoundException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @Test
    void handleJsonProcessingException_badRequestWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = Mockito.mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleJsonProcessingException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidFilterException_badRequestWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new InvalidFilterException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleInvalidFilterException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceConstraintViolationException_badRequestWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new ResourceConstraintViolationException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleResourceConstraintViolationException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUnknownResourcePropertyException_badRequestWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new UnknownResourcePropertyException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleUnknownResourcePropertyException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePersistenceException_badRequestWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new PersistenceException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handlePersistenceException(exception);
        var msg = "There was an error persisting data. Payloads must be valid.";
        assertResponseData(response, msg, HttpStatus.BAD_REQUEST);
    }


    private void assertResponseData(ResponseEntity<ApiErrorResponse> response, String message, HttpStatus httpStatus) {
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }



}
