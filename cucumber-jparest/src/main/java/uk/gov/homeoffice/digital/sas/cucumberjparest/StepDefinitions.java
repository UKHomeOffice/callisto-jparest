package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.response.Response;

/**
 * 
 * Step definitions to be included in the published cucumber-jparest package
 * 
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = JpaTestContext.class)
public class StepDefinitions {

    class ObjectObjectMap extends HashMap<Object, Object> {
    }

    private final PersonaManager personaManager;
    private final HttpResponseManager httpResponseManager;
    private final JpaRestApiClient jpaRestApiClient;

    /**
     * 
     * Checks that the objectUnderTest contains the given fields
     * 
     * @param objectUnderTest The object to check
     * @param fields          The fields the objectUnderTest should contain
     */
    private void objectContainsFields(Map<Object, Object> objectUnderTest, List<String> fields) {
        SoftAssertions softly = new SoftAssertions();
        fields.forEach((field) -> {
            softly
                    .assertThat(objectUnderTest)
                    .withFailMessage("Expected the object to contain the field '%s'", field)
                    .containsKey(field);
        });
        softly.assertAll();
    }

    /**
     * 
     * Checks that the objectUnderTest does not contain the given fields
     * 
     * @param objectUnderTest The object to check
     * @param fields          The fields the objectUnderTest should not contain
     */
    private void objectDoesNotContainFields(Map<Object, Object> objectUnderTest, List<String> fields) {
        SoftAssertions softly = new SoftAssertions();
        fields.forEach((field) -> {
            softly
                    .assertThat(objectUnderTest)
                    .withFailMessage("Expected the object to not contain the field '%s'", field)
                    .doesNotContainKey(field);
        });
        softly.assertAll();
    }

    @Autowired
    public StepDefinitions(PersonaManager personaManager, HttpResponseManager httpResponseManager,
            JpaRestApiClient jpaRestApiClient) {
        this.personaManager = Objects.requireNonNull(personaManager, "personaManager must not be null");
        this.httpResponseManager = Objects.requireNonNull(httpResponseManager, "httpResponseManager must not be null");
        this.jpaRestApiClient = Objects.requireNonNull(jpaRestApiClient, "jpaRestApiClient must not be null");
    }

    /**
     * 
     * Creates a new persona for the given name
     * 
     * @param name The name of the persona to create
     */
    @Given("^(?:the )?(\\S*) is a user$")
    public void persona_is_a_user(String name) {
        personaManager.createPersona(name);
    }

    /**
     * 
     * Creates a payload from the specified file and posts it to
     * the the endpoint exposed for the given resource.
     * 
     * @param persona      The persona to use for auth context
     * @param resource     The type of resource to be created
     * @param fileContents The file contents of the specified file
     * @param service      There service to use
     */
    @When("{persona} creates {word} from the {filecontents}{service}")
    public void persona_creates_resource_from_the_file_in_the_service(Persona persona, String resource,
            String fileContents, String service) {

        JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.Create(persona, service, resource, fileContents);

        this.httpResponseManager.addResponse(apiResponse.getBaseResourceURL(), apiResponse.getResponse());

    }

    /**
     * 
     * Retrieves the specified resource type from the given service.
     * 
     * @param persona  The persona to use for auth context
     * @param resource The type of resources to be retrieved
     * @param service  There service to use
     */
    @When("{persona} retrieves {word}{service}")
    public void persona_retrieves_resources_from_the_service(Persona persona, String resource, String service) {
        JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.Retrieve(persona, service, resource, null);

        this.httpResponseManager.addResponse(apiResponse.getBaseResourceURL(), apiResponse.getResponse());
    }

    /**
     * 
     * Makes a GET request to the service with the given path
     * 
     * @param persona The persona to use for auth context
     * @param path    The URL to request
     * @param service There service to use
     */
    @When("{persona} successfully GETs {string}{service}")
    public void someone_successfully_gets_url_from_service(Persona persona, String path, String service) {
        JpaRestApiResponse apiResponse = this.jpaRestApiClient.Get(persona, service, path);

        this.httpResponseManager.addResponse(apiResponse.getUrl(), apiResponse.getResponse());
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
    @Then("the last response should contain the fields")
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
    @Then("the last response should not contain the fields")
    public void the_last_response_should_not_contain_fields(List<String> fields) {
        var root = this.httpResponseManager.getLastResponse().getBody().jsonPath().getMap("");
        objectDoesNotContainFields(root, fields);
    }

    /**
     *
     * Checks that a resource contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {object_to_test} should contain the fields")
    public void the_object_should_contain_fields(Map<Object, Object> objectUnderTest, List<String> fields) {
        objectContainsFields(objectUnderTest, fields);
    }

    /**
     *
     * Checks that a resource does not contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {object_to_test} should not contain the fields")
    public void the_object_should_not_contain_fields(Map<Object, Object> objectUnderTest, List<String> fields) {
        objectDoesNotContainFields(objectUnderTest, fields);
    }

    /**
     * 
     * Matchs when a file is specified and returns the contents
     * of the specified file
     * 
     * @param path The path of the file
     * @return String The contents of the file
     */
    @ParameterType("file '([^']*)'")
    public String filecontents(String path) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(path).getFile());
            String data = Files.contentOf(file, "UTF-8");
            return data;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("File doesn't exist " + path);
        }
    }

    /**
     * 
     * Gets the persona for the given name
     * 
     * @param name The name of the persona
     * @return Persona The persona associated with the given name
     */
    @ParameterType("(?:the )?(\\S*)")
    public Persona persona(String name) {
        return this.personaManager.getPersona(name);
    }

    /**
     * 
     * Retrieves the location of the given service.
     * 
     * @param name The name of the service
     * @return String The URL of the service
     */
    @ParameterType("(?: (?:from|in) the (\\S*) service)")
    public String service(String name) {
        return name;
    }

    /**
     * 
     * Extracts a specific object from a specific response
     * 
     * @param objectPosition   The position of the resource to return
     * @param resourceName     The name of the type of resource to extract
     * @param responsePosition The ordinal of the response to retrieve the resource
     *                         from
     * @param path             The path specified in the request when retrieve
     *                         resources from a GET request (Optional)
     * @param service          The service the request was made to
     * @return Map<Object, Object>
     */
    @ParameterType("(?:last|(?:(\\d+)(?:st|nd|rd|th))) of the (\\S*) in the (?:last|(?:(\\d+)(?:st|nd|rd|th))) (?:\\\"([^\\\"]*)\\\" )?response from the (\\S*) service")
    public Map<Object, Object> object_to_test(String objectPosition,
            String resourceName, String responsePosition, String path, String service) {

        URL url;
        if (path == null || path.isEmpty()) {
            url = this.jpaRestApiClient.GetResourceURL(service, resourceName);
        } else {
            url = this.jpaRestApiClient.GetServiceURL(service, path);
        }

        int responseIndex = getIndex(responsePosition);
        Response response = this.httpResponseManager.getResponse(url, responseIndex);
        List<Map<Object, Object>> items = response.getBody().jsonPath().get("items");
        int objectIndex = getIndex(objectPosition);
        if (objectIndex == -1) {
            objectIndex = items.size() - 1;
        }

        return items.get(objectIndex);
    }

    /**
     * 
     * Converts positional string last, 1st, 2nd, 23rd, 30th etc
     * to a zero based ordinal integer.
     * 
     * The word last is converted to a -1
     * 
     * @param responsePosition
     * @return int
     */
    private int getIndex(String responsePosition) {
        if (responsePosition == null) {
            return -1;
        }
        return Integer.parseInt(responsePosition);
    }

}
