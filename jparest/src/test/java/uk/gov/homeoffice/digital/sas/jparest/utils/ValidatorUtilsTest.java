package uk.gov.homeoffice.digital.sas.jparest.utils;

import org.junit.jupiter.api.Test;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorUtilsTest {


    @Test
    void initValidator_validatorFactoryIsNotNull_validatorIsReturned() {
        assertThat(ValidatorUtils.initValidator()).isNotNull().isInstanceOf(Validator.class);
    }



    @Test
    void validateAndThrowIfErrorsExist_constraintViolationsExists_resourceConstraintViolationExceptionThrown() {
        var entity = new DummyEntityD();
        var validator = ValidatorUtils.initValidator();
        assertThatThrownBy(
                () -> ValidatorUtils.validateAndThrowIfErrorsExist(validator, entity))
                .isInstanceOf(ResourceConstraintViolationException.class);
    }


    @Test
    void validateAndThrowIfErrorsExist_constrainViolationsExist_validationErrorMessagesAreGroupedAndDisplayedForEachProperty() {

        var entity = new DummyEntityD();
        entity.setTelephone("-123456");
        var validator = ValidatorUtils.initValidator();

        assertThatThrownBy(
                () -> ValidatorUtils.validateAndThrowIfErrorsExist(validator, entity))
                .isInstanceOf(ResourceConstraintViolationException.class)
                .hasMessageContainingAll(
                        "description has the following error(s): must not be empty",
                        "telephone has the following error(s): ",
                        "numeric value out of bounds (<5 digits>.<0 digits> expected)", "must be greater than 0");
    }

}
