package uk.gov.homeoffice.digital.sas.jparest.utils;

import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

import javax.validation.ConstraintViolation;
import javax.validation.NoProviderFoundException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class EntityValidator {

    private static final Logger LOGGER = Logger.getLogger(EntityValidator.class.getName());
    private Validator validator = null;

    public EntityValidator() {
        try {
            var factory = Validation.buildDefaultValidatorFactory();
            if (factory != null) {
                this.validator = factory.getValidator();
            }
        } catch (NoProviderFoundException ex) {
            LOGGER.log(Level.WARNING, "No validation provider available", ex);
        }
    }


    public void validateAndThrowIfErrorsExist(Object objectToValidate) {
        if (this.validator != null) {
            var constraintViolations = this.validator.validate(objectToValidate);

            if (!constraintViolations.isEmpty()) {
                throw new ResourceConstraintViolationException(createResourceConstraintViolationMessage(constraintViolations));
            }
        }
    }


    private String createResourceConstraintViolationMessage(Set<ConstraintViolation<Object>> constraintViolations) {

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
