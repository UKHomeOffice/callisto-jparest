package uk.gov.homeoffice.digital.sas.jparest.utils;

import lombok.experimental.UtilityClass;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

import javax.validation.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


@UtilityClass
public class ValidatorUtils {

    private static final Logger LOGGER = Logger.getLogger(ValidatorUtils.class.getName());

    public static Validator initValidator() {
        try {
            var factory = Validation.buildDefaultValidatorFactory();
            if (factory != null) return factory.getValidator();
        } catch (NoProviderFoundException ex) {
            LOGGER.log(Level.WARNING, "No validation provider available", ex);
        }
        return null;
    }


    public static void validateAndThrowIfErrorsExist(Validator validator, Object objectToValidate) {
        if (validator != null) {
            var constraintViolations = validator.validate(objectToValidate);
            if (!constraintViolations.isEmpty()) {
                throw new ResourceConstraintViolationException(createResourceConstraintViolationMessage(constraintViolations));
            }
        }
    }


    private static String createResourceConstraintViolationMessage(Set<ConstraintViolation<Object>> constraintViolations) {

        return constraintViolations.stream()
                .collect(groupingBy(ConstraintViolation::getPropertyPath))
                .entrySet().stream()
                .map(entry -> {
                    var propertyErrors = entry.getValue().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                    return String.format("%s has the following error(s): %s", entry.getKey().toString(), propertyErrors);
                })
                .collect(Collectors.joining(". "));
    }


}
