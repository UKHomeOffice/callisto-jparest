package uk.gov.homeoffice.digital.sas.jparest.validation;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.springframework.test.util.AssertionErrors.assertTrue;

class EntityValidatorTest {

    @AfterAll
    static void clearInlineMocks() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void validateAndThrowIfErrorExists_validatorIsNullAndConstraintViolationsExist_NoResourceConstraintViolationExceptionExceptionThrown() {
        var entity = new DummyEntityD();
        var validationMockedStatic = Mockito.mockStatic(Validation.class);
        validationMockedStatic.when(Validation::buildDefaultValidatorFactory).thenReturn(null);

        var entityValidator =  new EntityValidator();
        assertThatNoException().isThrownBy(() -> entityValidator.validateAndThrowIfErrorsExist(entity));
        validationMockedStatic.close();
    }

    @Test
    void validateAndThrowIfErrorsExist_constraintViolationsExists_resourceConstraintViolationExceptionThrown() {
        var entity = new DummyEntityD();

        var entityValidator =  new EntityValidator();
        assertThatExceptionOfType(ResourceConstraintViolationException.class)
            .isThrownBy(() -> entityValidator.validateAndThrowIfErrorsExist(entity));
    }

    @Test
    void validateAndThrowIfErrorsExist_constrainViolationsExist_validationErrorMessagesAreGroupedAndDisplayedForEachProperty() {

        var entity = new DummyEntityD();
        entity.setTelephone("-123456");

        var entityValidator = new EntityValidator();

        Throwable thrown = catchThrowable(() -> entityValidator.validateAndThrowIfErrorsExist(entity));

        assertThat(thrown).isInstanceOf(ResourceConstraintViolationException.class);
        var errorResponse = ((ResourceConstraintViolationException) thrown).getErrorResponse();

        JSONObject telephoneError = null;
        JSONObject descriptionError = null;
        for(var i = 0 ; i < errorResponse.length ; i++) {
            var error = (JSONObject) errorResponse[i];
            if (error.get("field").equals("telephone")) {
                telephoneError = error;
            } else {
                descriptionError = error;
            }
        }


        assertThat(telephoneError.get("field")).isEqualTo("telephone");
        assertThat(((String)telephoneError.get("message")).contains("numeric value out of bounds (<5 digits>.<0 digits> expected)")).isTrue();
        assertThat(((String)telephoneError.get("message")).contains("must be greater than 0")).isTrue();

        assertThat(descriptionError.get("field")).isEqualTo("description");
        assertThat(descriptionError.get("message")).isEqualTo("must not be empty");
    }

}
