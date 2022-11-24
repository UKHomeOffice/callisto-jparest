package uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.expectations;

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
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Provides methods used to assert expectations against given objects.
 */
public class Assertions {

  private static final Pattern FIELD_PATH_IS_AN_ARRAY = Pattern.compile("(?=(.*)\\[(\\d+)\\]$).*");
  private static final Pattern FIELD_PATH = Pattern.compile("(.*)(\\.|^)(.*)");

  private final BeanExpressionContext rootObject;
  private final ConfigurableBeanFactory beanFactory;
  
  @Autowired
  public Assertions(ConfigurableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    this.rootObject = new BeanExpressionContext(beanFactory, null);
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
   * @param fieldExpectations    A table of expectations to assert
   */
  public void objectMeetsExpectations(JsonPath objectUnderTest,
      List<FieldExpectation> fieldExpectations,
      ObjectMapper objectMapper) {
    SoftAssertions softly = new SoftAssertions();
    objectMeetsExpectations(objectUnderTest, fieldExpectations, objectMapper, softly);
    softly.assertAll();
  }

  /**
   * Softly asserts that the objectUnderTests meets the provided expectations.
   *
   * @param objectUnderTest JsonPath pointing to the object to assert against
   * @param fieldExpectations    A table of expectations to assert
   * @param softly          The SoftAssertions instance
   */
  @SuppressWarnings("squid:S5960")// Assertions are needed in this test library
  public void objectMeetsExpectations(JsonPath objectUnderTest,
      List<FieldExpectation> fieldExpectations, ObjectMapper objectMapper,
      @NonNull SoftAssertions softly) {
    fieldExpectations.forEach(expect -> {
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
        String pathCheck;
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
  public void headersMeetsExpectations(Headers headers, Map<String, String> expectations) {
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
  private void evaluateExpectation(Object testSubject, String expectation,
      @NonNull SoftAssertions softly) {
    // Assert the expectation
    try {
      // Create the evaluation context and set the variable and function
      StandardEvaluationContext context = new StandardEvaluationContext();
      context.setVariable("objectToTest", testSubject);
      context.setBeanResolver(new BeanFactoryResolver(this.beanFactory));
      context.addPropertyAccessor(new BeanExpressionContextAccessor());
  
      // The assertThat function has to be reflected because of type erasure
      // otherwise we would only be able to assert against objects
      Method assertThatMethod = MethodUtils.getMatchingAccessibleMethod(
          org.assertj.core.api.Assertions.class, "assertThat",
          testSubject.getClass());
      if (assertThatMethod == null) {
        softly.fail(
            "Unable to verify expectation. The org.assertj.core.api.Assertions class "
                + "contains no matching assertThat method for the type %s",
            testSubject.getClass());
      } else {
        context.registerFunction("assertThat", assertThatMethod);
      }

      // Construct an expression from the provided expectation using a reference
      // to the assertFunction to be resolved and the variable that
      // will represent the object under test.
      ExpressionParser expressionParser = new SpelExpressionParser();
      Expression expression = expressionParser
          .parseExpression("#assertThat(#objectToTest)." + expectation);

      // Execute the expression and capture any EvaluationException to determine
      // how the expectation failed
      expression.getValue(context, rootObject);

    } catch (SpelParseException ex) {
      softly.fail("Invalid expectation: " + expectation);
    } catch (EvaluationException ex) {
      // If an expectation executed correctly but failed retrieve the underlying cause
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
