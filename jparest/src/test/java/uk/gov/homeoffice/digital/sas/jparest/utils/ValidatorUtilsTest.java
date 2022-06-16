package uk.gov.homeoffice.digital.sas.jparest.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ValidatorUtilsTest {

    @AfterAll
    static void clearInlineMocks() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void validateAndThrowIfErrorExists_validatorIsNullAndConstraintViolationsExist_NoResourceConstraintViolationExceptionExceptionThrown() {
        var entity = new DummyEntityD();
        var validationMockedStatic = Mockito.mockStatic(Validation.class);
        validationMockedStatic.when(Validation::buildDefaultValidatorFactory).thenReturn(null);

        var validatorUtils =  new ValidatorUtils();
        assertThatNoException().isThrownBy(() -> validatorUtils.validateAndThrowIfErrorsExist(entity));
        validationMockedStatic.close();
    }

    @Test
    void validateAndThrowIfErrorsExist_constraintViolationsExists_resourceConstraintViolationExceptionThrown() {
        var entity = new DummyEntityD();

        var validatorUtils =  new ValidatorUtils();
        assertThatExceptionOfType(ResourceConstraintViolationException.class)
            .isThrownBy(() -> validatorUtils.validateAndThrowIfErrorsExist(entity));
    }

    @Test
    void validateAndThrowIfErrorsExist_constrainViolationsExist_validationErrorMessagesAreGroupedAndDisplayedForEachProperty() {

        var entity = new DummyEntityD();
        entity.setTelephone("-123456");

        var validatorUtils = new ValidatorUtils();

        assertThatExceptionOfType(ResourceConstraintViolationException.class)
            .isThrownBy(() -> validatorUtils.validateAndThrowIfErrorsExist(entity))
            .withMessageContainingAll(
                "description has the following error(s): must not be empty",
                "telephone has the following error(s): ",
                "numeric value out of bounds (<5 digits>.<0 digits> expected)", "must be greater than 0");
    }

}
