package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiErrorResponse;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;


import com.fasterxml.jackson.core.JsonProcessingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


class ApiResponseExceptionHandlerTest {

    private static final String ERROR_MESSAGE = "error message";

    @Test
    void handleIllegalArgumentException_badRequestWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new IllegalArgumentException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleIllegalArgumentException(exception);
        assertResponseData(response, exception, HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceNotFoundException_notFoundWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new ResourceNotFoundException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleResourceNotFoundException(exception);
        assertResponseData(response, exception, HttpStatus.NOT_FOUND);
    }

    @Test
    void handleJsonProcessingException_badRequestWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = Mockito.mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleJsonProcessingException(exception);
        assertResponseData(response, exception, HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidFilterException_responseWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new InvalidFilterException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleInvalidFilterException(exception);
        assertResponseData(response, exception, HttpStatus.BAD_REQUEST);

    }

    private void assertResponseData(ResponseEntity<ApiErrorResponse> response, Exception exception, HttpStatus httpStatus) {
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }



}
