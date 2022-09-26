package uk.gov.homeoffice.digital.sas.cucumberjparest.StepDefinitions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.ExpectationUtils.headersMeetsExpectations;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.ExpectationUtils.objectContainsFields;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.ExpectationUtils.objectDoesNotContainFields;
import static uk.gov.homeoffice.digital.sas.cucumberjparest.ExpectationUtils.objectMeetsExpectations;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.en.Then;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Expectation;
import uk.gov.homeoffice.digital.sas.cucumberjparest.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Resource;

public class AssertionSteps {

    private final HttpResponseManager httpResponseManager;
    private final ObjectMapper objectMapper;

    @Autowired
    public AssertionSteps(@NonNull HttpResponseManager httpResponseManager, @NonNull ObjectMapper objectMapper) {
        this.httpResponseManager = httpResponseManager;
        this.objectMapper = objectMapper;
    }

    /**
     * 
     * Validates the last responses status code against the given value
     * 
     * @param expectedCode The expected status code of the last response
     */
    @Then("the last response should have a status code of {int}")
    public void the_last_response_should_have_a_status_code_of(Integer expectedCode) {
        assertThat(this.httpResponseManager.getLastResponse().statusCode()).isEqualTo(expectedCode);
    }

    @Then("the last response body should not be empty")
    public void the_last_response_body_should_not_be_empty() {
        assertThat(this.httpResponseManager.getLastResponse().body().asString())
                .isNotNull()
                .isNotEqualTo("");
    }

    @Then("the last response body should be empty")
    public void the_last_response_body_should_be_empty() {
        assertThat(this.httpResponseManager.getLastResponse().body().asString())
                .isNotNull()
                .isEqualTo("");
    }

    /**
     * 
     * Checks that the response contains the given fields
     * 
     * @param fields The fields to check the response for
     */
    @Then("the last response body should contain the fields")
    public void the_last_response_should_contain_fields(List<String> fields) {
        var root = this.httpResponseManager.getLastResponse().getBody().jsonPath().getMap("");
        objectContainsFields(root, fields);
    }

    /**
     * 
     * Checks that the response does not contains the given fields
     * 
     * @param fields The fields to check the response for
     */
    @Then("the last response body should not contain the fields")
    public void the_last_response_should_not_contain_fields(List<String> fields) {
        var root = this.httpResponseManager.getLastResponse().getBody().jsonPath().getMap("");
        objectDoesNotContainFields(root, fields);
    }

    /**
     * 
     * Assert expectations against the last response
     * 
     * @param expectations A table of expectations to assert
     */
    @Then("the last response body should contain")
    public void the_last_response_should_contain(List<Expectation> expectations) {
        var root = this.httpResponseManager.getLastResponse().getBody().jsonPath();
        objectMeetsExpectations(root, expectations, this.objectMapper);
    }

    /**
     * 
     * Assert expectations against the last response's headers
     * 
     * @param expectations A table of expectations to assert
     */
    @Then("the last response should contain the headers")
    public void the_last_response_should_contain_the_headers(Map<String, String> expectations) {
        var response = this.httpResponseManager.getLastResponse();
        headersMeetsExpectations(response.getHeaders(), expectations);
    }

    /**
     *
     * Checks that a resource contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {resource} should contain the fields")
    public void the_object_should_contain_fields(Resource objectUnderTest, List<String> fields) {
        objectContainsFields(objectUnderTest.getJsonPath().getMap(""), fields);
    }

    /**
     *
     * Checks that a resource does not contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {resource} should not contain the fields")
    public void the_object_should_not_contain_fields(Resource objectUnderTest, List<String> fields) {
        objectDoesNotContainFields(objectUnderTest.getJsonPath().getMap(""), fields);
    }

    /**
     * 
     * Retrieves a specific resource from a list of resources from the
     * specified response and applies the given expectations to that resource.
     * 
     * @param objectUnderTest The object to test
     * @param expectations    A table of expectations to assert
     */
    @Then("the {resource} should contain")
    public void the_object_should_contain(Resource objectUnderTest, List<Expectation> expectations) {
        objectMeetsExpectations(objectUnderTest.getJsonPath(), expectations, this.objectMapper);
    }

    /**
     * 
     * Compares 2 resources to see if they are equal
     * 
     * @param objectToCompare     The object to be compared
     * @param objectToCompareWith The object to compare with
     */
    @Then("the {resource} should equal the {resource}")
    public void the_object_should_contain(Resource objectToCompare, Resource objectToCompareWith) {
        assertThat(objectToCompare.getJsonPath().getMap("")).isEqualTo(objectToCompareWith.getJsonPath().getMap(""));
    }

    /**
     * 
     * Retrieves all resources from the specified response and applies
     * the given expectations to each resource.
     * 
     * @param objectsUnderTest The object to test
     * @param expectations     A table of expectations to assert against each
     *                         resource
     */
    @Then("{each_resource} should contain")
    public void the_objects_should_contain(Resource objectsUnderTest, List<Expectation> expectations) {

        SoftAssertions softly = new SoftAssertions();

        var itemsSize = objectsUnderTest.getJsonPath().getInt("size()");
        for (int i = 0; i < itemsSize; i++) {
            objectsUnderTest.getJsonPath().setRootPath("items[" + i + "]");
            objectMeetsExpectations(objectsUnderTest.getJsonPath(), expectations, this.objectMapper, softly);
        }

        softly.assertAll();
    }

}
