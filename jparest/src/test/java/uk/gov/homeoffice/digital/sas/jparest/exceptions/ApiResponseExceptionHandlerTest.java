package uk.gov.homeoffice.digital.sas.jparest.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiErrorResponse;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.exceptionhandling.ApiResponseExceptionHandler;
import uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper;

import javax.persistence.PersistenceException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.homeoffice.digital.sas.jparest.utils.ConstantHelper.SERVER_ERROR;


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
    void handlePersistenceException_internalServerErrorWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new PersistenceException(ERROR_MESSAGE);
        var response = apiResponseExceptionHandler.handlePersistenceException(exception);
        assertResponseData(response, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleTypeMismatchException_badRequestWithErrorDataIsReturned() {

        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new TypeMismatchException(1, UUID.class);
        var response = apiResponseExceptionHandler.handleTypeMismatchException(exception);
        var msg = "Parameters must be of the relevant types specified by the API";
        assertResponseData(response, msg, HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleUnexpectedQueryResultException_internalServerErrorWithErrorDataIsReturned() {
        var apiResponseExceptionHandler = new ApiResponseExceptionHandler();
        var exception = new UnexpectedQueryResultException(UUID.randomUUID());
        var response = apiResponseExceptionHandler.handleUnexpectedQueryResultException(exception);
        assertResponseData(response, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
                Arguments.of(new TenantIdMismatchException())
        );
    }
}
