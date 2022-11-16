package uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.expectations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents an entry in a scenario table that
 * expresses an expectation of a field existing
 * to be of a given type and match a value.
 * 
 */
@Getter
@Setter
@AllArgsConstructor
public class FieldExpectation {

  @NonNull
  private String field;
  @NonNull
  private Class<?> type;
  @NonNull
  private String expectation;
}
