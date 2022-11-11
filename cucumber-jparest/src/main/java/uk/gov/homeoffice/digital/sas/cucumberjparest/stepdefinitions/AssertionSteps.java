package uk.gov.homeoffice.digital.sas.cucumberjparest.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.utils.ExpectationUtils.headersMeetsExpectations;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.utils.ExpectationUtils.objectContainsFields;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.utils.ExpectationUtils.objectDoesNotContainFields;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.utils.ExpectationUtils.objectMeetsExpectations;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.assertj.core.api.SoftAssertions;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Expectation;
import uk.gov.homeoffice.digital.sas.cucumberjparest.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Resource;

/**
 * Provides steps related to making assertions against responses and the json objects within them.
 */
@SuppressWarnings("squid:S5960")// Assertions are needed in this test library
public class AssertionSteps {

  private final HttpResponseManager httpResponseManager;
  private final ObjectMapper objectMapper;

  public AssertionSteps(@NonNull HttpResponseManager httpResponseManager,
      @NonNull ObjectMapper objectMapper) {
    this.httpResponseManager = httpResponseManager;
    this.objectMapper = objectMapper;
  }

  /**
   * Validates the last responses status code against the given value.
   *
   * @param expectedCode The expected status code of the last response
   */
  @Then("the last response should have a status code of {int}")
  public void theLastResponseShouldHaveStatusCodeOf(Integer expectedCode) {
    assertThat(this.httpResponseManager.getLastResponse().statusCode())
        .isEqualTo(expectedCode);
  }

  @Then("the last response body should not be empty")
  public void theLastResponseBodyShouldNotBeEmpty() {
    assertThat(this.httpResponseManager.getLastResponse().body().asString())
        .isNotNull()
        .isNotEqualTo("");
  }

  @Then("the last response body should be empty")
  public void theLastResponseBodyShouldBeEmpty() {
    assertThat(this.httpResponseManager.getLastResponse().body().asString())
        .isNotNull()
        .isEqualTo("");
  }

  /**
   * Checks that the response contains the given fields.
   *
   * @param fields The fields to check the response for
   */
  @Then("the last response body should contain the fields")
  public void theLastResponseShouldContainFields(List<String> fields) {
    var root = this.httpResponseManager.getLastResponse().getBody().jsonPath().getMap("");
    objectContainsFields(root, fields);
  }

  /**
   * Checks that the response does not contain the given fields.
   *
   * @param fields The fields to check the response for
   */
  @Then("the last response body should not contain the fields")
  public void theLastResponseShouldNotContainFields(List<String> fields) {
    var root = this.httpResponseManager.getLastResponse().getBody().jsonPath().getMap("");
    objectDoesNotContainFields(root, fields);
  }

  /**
   * Assert expectations against the last response.
   *
   * @param expectations A table of expectations to assert
   */
  @Then("the last response body should contain")
  public void theLastResponseShouldContain(List<Expectation> expectations) {
    var root = this.httpResponseManager.getLastResponse().getBody().jsonPath();
    objectMeetsExpectations(root, expectations, this.objectMapper);
  }

  /**
   * Assert expectations against the last response's headers.
   *
   * @param expectations A table of expectations to assert
   */
  @Then("the last response should contain the headers")
  public void theLastResponseShouldContainTheHeaders(Map<String, String> expectations) {
    var response = this.httpResponseManager.getLastResponse();
    headersMeetsExpectations(response.getHeaders(), expectations);
  }

  /**
   * Checks that a resource contains the given fields.
   *
   * @param objectUnderTest The object to test
   * @param fields          The fields to check the response for
   */
  @Then("the {resource} should contain the fields")
  public void theObjectShouldContainFields(Resource objectUnderTest, List<String> fields) {
    objectContainsFields(objectUnderTest.getJsonPath().getMap(""), fields);
  }

  /**
   * Checks that a resource does not contain the given fields.
   *
   * @param objectUnderTest The object to test
   * @param fields          The fields to check the response for
   */
  @Then("the {resource} should not contain the fields")
  public void theObjectShouldNotContainFields(Resource objectUnderTest, List<String> fields) {
    objectDoesNotContainFields(objectUnderTest.getJsonPath().getMap(""), fields);
  }

  /**
   * Retrieves a specific resource from a list of resources from the specified response and applies
   * the given expectations to that resource.
   *
   * @param objectUnderTest The object to test
   * @param expectations    A table of expectations to assert
   */
  @Then("the {resource} should contain")
  public void theObjectShouldContain(Resource objectUnderTest, List<Expectation> expectations) {
    objectMeetsExpectations(objectUnderTest.getJsonPath(), expectations, this.objectMapper);
  }

  /**
   * Compares 2 resources to see if they are equal.
   *
   * @param objectToCompare     The object to be compared
   * @param objectToCompareWith The object to compare with
   */
  @Then("the {resource} should equal the {resource}")
  public void theResourceShouldMatchResource(Resource objectToCompare,
      Resource objectToCompareWith) {
    assertThat(objectToCompare.getJsonPath().getMap("")).isEqualTo(
        objectToCompareWith.getJsonPath().getMap(""));
  }

  /**
   * Compares 2 resources to see if they are not equal.
   *
   * @param objectToCompare     The object to be compared
   * @param objectToCompareWith The object to compare with
   */
  @Then("the {resource} should not be equal to the {resource}")
  public void theResourceShouldNotMatchResource(Resource objectToCompare,
      Resource objectToCompareWith) {
    assertThat(objectToCompare.getJsonPath().getMap("")).isNotEqualTo(
        objectToCompareWith.getJsonPath().getMap(""));
  }

  /**
   * Retrieves all resources from the specified response and applies the given expectations to each
   * resource.
   *
   * @param objectsUnderTest The object to test
   * @param expectations     A table of expectations to assert against each resource
   */
  @Then("{eachResource} should contain")
  public void theObjectsShouldContain(Resource objectsUnderTest,
      List<Expectation> expectations) {

    SoftAssertions softly = new SoftAssertions();

    var itemsSize = objectsUnderTest.getJsonPath().getInt("size()");
    for (int i = 0; i < itemsSize; i++) {
      objectsUnderTest.getJsonPath().setRootPath("items[" + i + "]");
      objectMeetsExpectations(objectsUnderTest.getJsonPath(), expectations, this.objectMapper,
          softly);
    }

    softly.assertAll();
  }

}
