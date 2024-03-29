package uk.gov.homeoffice.digital.sas.jparest.validation;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.NoProviderFoundException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.StructuredError;

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
        throw new ResourceConstraintViolationException(
            createStructuredErrors(constraintViolations));
      }
    }
  }

  private static ArrayList<StructuredError> createStructuredErrors(
          Set<ConstraintViolation<Object>> constraintViolations) {

    var constraintViolation = constraintViolations.iterator().next();
    var hibernateConstraintViolation = constraintViolation.unwrap(
        HibernateConstraintViolation.class
    );

    return constraintViolations.stream()
            .collect(groupingBy(ConstraintViolation::getPropertyPath))
            .entrySet().stream()
            .map(entry -> {
              var propertyErrors = entry.getValue().stream()
                      .map(ConstraintViolation::getMessage)
                      .collect(Collectors.joining(", "));

              return new StructuredError(entry.getKey().toString(),
                  propertyErrors,
                  hibernateConstraintViolation.getDynamicPayload(ArrayList.class));
            }).collect(toCollection(ArrayList::new));
  }
}
