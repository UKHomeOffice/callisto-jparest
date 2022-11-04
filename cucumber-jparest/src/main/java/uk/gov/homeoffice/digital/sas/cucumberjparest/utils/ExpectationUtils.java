package uk.gov.homeoffice.digital.sas.cucumberjparest.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Expectation;

/**
 * Provides methods used to assert expectations against given objects.
 */
public class ExpectationUtils {

  private static final Pattern FIELD_PATH_IS_AN_ARRAY = Pattern.compile("(?=(.*)\\[(\\d+)\\]$).*");
  private static final Pattern FIELD_PATH = Pattern.compile("(.*)(\\.|^)(.*)");

  private ExpectationUtils() {
  }

  /**
   * Checks that the objectUnderTest contains the given fields.
   *
   * @param objectUnderTest The object to check
   * @param fields          The fields the objectUnderTest should contain
   */
  public static void objectContainsFields(Map<Object, Object> objectUnderTest,
      List<String> fields) {
    SoftAssertions softly = new SoftAssertions();
    fields.forEach(field ->
        softly
            .assertThat(objectUnderTest)
            .withFailMessage("Expected the object to contain the field '%s'", field)
            .containsKey(field)
    );
    softly.assertAll();
  }

  /**
   * Checks that the objectUnderTest does not contain the given fields.
   *
   * @param objectUnderTest The object to check
   * @param fields          The fields the objectUnderTest should not contain
   */
  public static void objectDoesNotContainFields(Map<Object, Object> objectUnderTest,
      List<String> fields) {
    SoftAssertions softly = new SoftAssertions();
    fields.forEach(field ->
        softly
          .assertThat(objectUnderTest)
          .withFailMessage("Expected the object to not contain the field '%s'", field)
          .doesNotContainKey(field));
    softly.assertAll();
  }

  /**
   * A convenience method for
   * {@link ExpectationUtils#objectMeetsExpectations(JsonPath, List, ObjectMapper, SoftAssertions)}.
   * It creates an instance of SoftAssertions and calls the wrapped method then calls
   * {@link SoftAssertions#assertAll()}
   *
   * @param objectUnderTest JsonPath pointing to the object to assert against
   * @param expectations    A table of expectations to assert
   */
  public static void objectMeetsExpectations(JsonPath objectUnderTest,
      List<Expectation> expectations,
      ObjectMapper objectMapper) {
    SoftAssertions softly = new SoftAssertions();
    objectMeetsExpectations(objectUnderTest, expectations, objectMapper, softly);
    softly.assertAll();
  }

  /**
   * Softly asserts that the objectUnderTests meets the provided expectations.
   *
   * @param objectUnderTest JsonPath pointing to the object to assert against
   * @param expectations    A table of expectations to assert
   * @param softly          The SoftAssertions instance
   */
  public static void objectMeetsExpectations(JsonPath objectUnderTest,
      List<Expectation> expectations, ObjectMapper objectMapper, @NonNull SoftAssertions softly) {

    expectations.forEach(expect -> {
      var field = expect.getField();

      /**
       * Assert field exists
       *
       * In order to support nested properties in the field we can't depend on the
       * get method because we cant distinguish between a key that is not present and
       * key that is present but has a null value. We can inject containsKey into the
       * path and also catch IllegalArgumentException which are caused when any
       * parent part of the path doesn't exist.
       * e.g.
       * | field | JsonPath to test existence
       * | description | containsKey('description')
       * | summary | containsKey('summary')
       * | items[0].description | items[0].containsKey('description')
       * | items[0].summary | items[0].containsKey('summary')
       *
       * (.*)(\.|^)(.*)
       * $1$2containsKey('$3')
       *
       *
       */
      softly.assertThatCode(() -> {
        String pathCheck = null;
        if (field.endsWith("]")) {
          pathCheck = FIELD_PATH_IS_AN_ARRAY.matcher(field).replaceAll("$1.size() > $2");
        } else {
          pathCheck = FIELD_PATH.matcher(field).replaceAll("$1$2containsKey('$3')");
        }
        assertThat(objectUnderTest.getBoolean(pathCheck)).isTrue();
      })
          .withFailMessage("Expected the object to contain the field '%s'", field)
          .doesNotThrowAnyException();

      // Assert the expectation
      try {
        // Retrieve the typed object from the JsonPath and fail if the type doesn't
        // match
        var testSubject = objectMapper.convertValue(objectUnderTest.get(field), expect.getType());

        // Skip this if test subject doesn't exist, this will be caught in previous
        // assertion
        if (testSubject != null) {
          evaluateExpectation(testSubject, expect.getExpectation(), softly);
        }
      } catch (IllegalArgumentException ex) {
        softly.fail("Expected value to be of type '%s'", expect.getType());
      }
    });
  }

  /**
   * Asserts that the provider headers contains the provided expectations.
   *
   * @param headers      The headers from a response
   * @param expectations The expectations to assert against the headers
   */
  public static void headersMeetsExpectations(Headers headers, Map<String, String> expectations) {
    SoftAssertions softly = new SoftAssertions();
    expectations.forEach((headerName, expectation) -> {
      var header = headers.get(headerName);
      softly.assertThat(header).isNotNull();
      evaluateExpectation(headers.getValue(headerName), expectation, softly);
    });
    softly.assertAll();
  }

  /**
   * Softly evaluates the expectation against the provided test subject.
   *
   * @param testSubject The object to assert against
   * @param expectation The expectation
   * @param softly      The SoftAssertions instance
   */
  private static void evaluateExpectation(Object testSubject, String expectation,
      @NonNull SoftAssertions softly) {
    // Assert the expectation
    try {
      // Create the evaluation context and set the variable and function
      StandardEvaluationContext context = new StandardEvaluationContext();
      context.setVariable("objectToTest", testSubject);

      // The assertThat functiion has to be reflected because of type erasure
      // otherwise we would only be able to assert against objects
      Method assertThatMethod = MethodUtils.getMatchingAccessibleMethod(Assertions.class,
          "assertThat",
          testSubject.getClass());
      if (assertThatMethod == null) {
        softly.fail(
            "Unable to verify expectation. The org.assertj.core.api.Assertions class "
                + "contains no matching assertThat method for the type %s",
            testSubject.getClass());
      }

      // Construct an expression from the provided expectation using a reference
      // to the assertFunction to be resolved and the variable that
      // will represent the object under test.
      ExpressionParser expressionParser = new SpelExpressionParser();
      Expression expression = expressionParser
          .parseExpression("#assertThat(#objectToTest)." + expectation);

      context.registerFunction("assertThat", assertThatMethod);

      // Execute the expression and capture any EvaluationException to determine
      // how the expectation failed
      expression.getValue(context);

    } catch (SpelParseException ex) {
      softly.fail("Invalid expectation: " + expectation);
    } catch (EvaluationException ex) {
      // If an expectation exectued correctly but failed retrieve the underlying cause
      // to expose the failed expectation
      var cause = ex.getCause();
      if (cause != null) {
        softly.fail(ex.getCause().getMessage());
      } else {
        softly.fail(ex.getMessage());
      }
    }
  }
}
