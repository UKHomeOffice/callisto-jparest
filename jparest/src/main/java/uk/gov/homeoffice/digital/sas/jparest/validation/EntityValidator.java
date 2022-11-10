package uk.gov.homeoffice.digital.sas.jparest.validation;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.NoProviderFoundException;
import javax.validation.Validation;
import javax.validation.Validator;
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

  private static List<StructuredError> createStructuredErrors(
          Set<ConstraintViolation<Object>> constraintViolations) {

    var constraintViolation = constraintViolations.iterator().next();
    var hibernateConstraintViolation = constraintViolation.unwrap(
        HibernateConstraintViolation.class
    );
    var payload = hibernateConstraintViolation.getDynamicPayload(ArrayList.class);

    return constraintViolations.stream()
            .collect(groupingBy(ConstraintViolation::getPropertyPath))
            .entrySet().stream()
            .map(entry -> {
              var propertyErrors = entry.getValue().stream()
                      .map(ConstraintViolation::getMessage)
                      .collect(Collectors.joining(", "));

              return new StructuredError(entry.getKey().toString(),
                  propertyErrors,
                  payload);
            }).collect(Collectors.toList());
  }
}
