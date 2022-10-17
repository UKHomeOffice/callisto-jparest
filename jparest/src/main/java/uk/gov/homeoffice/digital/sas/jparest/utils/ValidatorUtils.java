package uk.gov.homeoffice.digital.sas.jparest.utils;

import static java.util.stream.Collectors.groupingBy;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.NoProviderFoundException;
import javax.validation.Validation;
import javax.validation.Validator;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;

public class ValidatorUtils {

  private static final Logger LOGGER = Logger.getLogger(ValidatorUtils.class.getName());
  private Validator validator = null;

  public ValidatorUtils() {
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
        throw new ResourceConstraintViolationException(
          createResourceConstraintViolationMessage(constraintViolations));
      }
    }
  }

  private static String createResourceConstraintViolationMessage(
      Set<ConstraintViolation<Object>> constraintViolations) {

    return constraintViolations.stream()
      .collect(groupingBy(ConstraintViolation::getPropertyPath))
      .entrySet().stream()
      .map(entry -> {
        var propertyErrors = entry.getValue().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        return String.format("%s has the following error(s): %s",
          entry.getKey().toString(), propertyErrors);
      })
      .collect(Collectors.joining(". "));
  }


}
