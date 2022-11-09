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

        var firstError = (JSONObject) errorResponse[0];
        assertThat(firstError.get("field")).isEqualTo("telephone");
        assertThat(firstError.get("message")).isEqualTo("numeric value out of bounds (<5 digits>.<0 digits> expected), must be greater than 0");

        var secondError = (JSONObject) errorResponse[1];
        assertThat(secondError.get("field")).isEqualTo("description");
        assertThat(secondError.get("message")).isEqualTo("must not be empty");
    }

}
