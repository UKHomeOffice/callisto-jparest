package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiErrorResponse;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;

import javax.persistence.PersistenceException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


class ApiResponseExceptionHandlerTest {

    private static final String ERROR_MESSAGE = "error message";

    @ParameterizedTest
    @MethodSource("exceptionTypes")
    void handleException_badRequestWithErrorDataIsReturned(Exception exception) {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var response = apiResponseExceptionHandler.handleException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleResourceNotFoundException_notFoundWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new ResourceNotFoundException(1);
        var response = apiResponseExceptionHandler.handleResourceNotFoundException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @Test
    void handlePersistenceException_badRequestWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new PersistenceException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handlePersistenceException(exception);
        var msg = "There was an error persisting data.";
        assertResponseData(response, msg, HttpStatus.BAD_REQUEST);
    }


    private void assertResponseData(ResponseEntity<ApiErrorResponse> response, String message, HttpStatus httpStatus) {
        assertThat(response.getStatusCode()).isEqualTo(httpStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    private static Stream<Arguments> exceptionTypes() {
        var jsonProcessingException = Mockito.mock(JsonProcessingException.class);
        when(jsonProcessingException.getMessage()).thenReturn(ERROR_MESSAGE);

        return Stream.of(
                Arguments.of(new IllegalArgumentException(ERROR_MESSAGE)),
                Arguments.of(jsonProcessingException),
                Arguments.of(new InvalidFilterException(ERROR_MESSAGE)),
                Arguments.of(new ResourceConstraintViolationException(ERROR_MESSAGE)),
                Arguments.of(new UnknownResourcePropertyException("unknownProperty", "resourceName")),
                Arguments.of(new TenantIdMismatchException(ERROR_MESSAGE))
        );
    }
}
