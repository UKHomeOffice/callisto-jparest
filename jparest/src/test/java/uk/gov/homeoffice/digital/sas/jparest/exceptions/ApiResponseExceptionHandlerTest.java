package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiErrorResponse;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


public class ApiResponseExceptionHandlerTest {

    private ApiResponseExceptionHandler apiResponseExceptionHandler;
    private Clock clock;

    private static final String ERROR_MESSAGE = "error message";

    @BeforeEach
    public void setUp() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        apiResponseExceptionHandler = new ApiResponseExceptionHandler(clock);
    }


    @Test
    void handleIllegalArgumentException_responseWithErrorDataIsReturned() {

        var exception = new IllegalArgumentException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleIllegalArgumentException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceNotFoundException_responseWithErrorDataIsReturned() {

        var exception = new ResourceNotFoundException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleResourceNotFoundException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @Test
    void handleIOException_responseWithErrorDataIsReturned() {

        var exception = new IOException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleIOException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleInvalidFilterException_responseWithErrorDataIsReturned() {

        var exception = new InvalidFilterException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleInvalidFilterException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceConstraintViolationException_responseWithErrorDataIsReturned() {

        var exception = new ResourceConstraintViolationException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleResourceConstraintViolationException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUnknownResourcePropertyException_responseWithErrorDataIsReturned() {

        var exception = new UnknownResourcePropertyException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handleUnknownResourcePropertyException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlePersistenceException_responseWithErrorDataIsReturned() {

        var exception = new PersistenceException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handlePersistenceException(exception);
        var msg = "There was an error persisting data. Payloads must be constructed correctly.";
        assertResponseData(response, msg, HttpStatus.BAD_REQUEST);
    }




    private void assertResponseData(ResponseEntity<ApiErrorResponse> response, String message, HttpStatus httpStatus) {
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHttpStatus()).isEqualTo(String.valueOf(httpStatus.value()));
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getTime()).isEqualTo(Date.from(clock.instant()).toString());
    }



}
