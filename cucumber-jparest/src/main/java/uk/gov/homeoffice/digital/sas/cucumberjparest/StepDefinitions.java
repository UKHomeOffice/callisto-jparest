package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.jparest.config.ObjectMapperConfig;

/**
 * 
 * Step definitions to be included in the published cucumber-jparest package
 * 
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = { JpaTestContext.class, ObjectMapperConfig.class })
public class StepDefinitions {

    private static final String FROM_IN_SERVICE = "(?: (?:from|in) the (\\S*) service)?";
    private static final Pattern FIELD_PATH_IS_AN_ARRAY = Pattern.compile("(?=(.*)\\[(\\d+)\\]$).*");
    private static final Pattern FIELD_PATH = Pattern.compile("(.*)(\\.|^)(.*)");

    class ObjectObjectMap extends HashMap<Object, Object> {
    }

    private final PersonaManager personaManager;
    private final HttpResponseManager httpResponseManager;
    private final JpaRestApiClient jpaRestApiClient;
    private final ScenarioState scenarioState;
    private final ObjectMapper objectMapper;

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

    /**
     * 
     * A convenience method for {@see #objectMeetsExpectations(SoftAssertions,
     * JsonPath, List)}
     * Creates an instance of SoftAssertions and calls the wrapped method
     * then calles {@see SoftAssertions#assertAll()}
     * 
     * @param objectUnderTest JsonPath pointing to the object to assert against
     * @param expectations    A table of expectations to assert
     */
    private void objectMeetsExpectations(JsonPath objectUnderTest, List<Expectation> expectations) {
        SoftAssertions softly = new SoftAssertions();
        objectMeetsExpectations(softly, objectUnderTest, expectations);
        softly.assertAll();
    }

    /**
     * 
     * Softly asserts that the objectUnderTests meets the provided expectations
     * 
     * @param softly          The SoftAssertions instance
     * @param objectUnderTest JsonPath pointing to the object to assert against
     * @param expectations    A table of expectations to assert
     */
    private void objectMeetsExpectations(@NonNull SoftAssertions softly, JsonPath objectUnderTest,
            List<Expectation> expectations) {

        expectations.forEach((expect) -> {
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
            }).withFailMessage("Expected the object to contain the field '%s'", field)
                    .doesNotThrowAnyException();

            // Assert the expectation
            try {
                // Construct an expression from the provided expectation using a reference
                // to the assertFunction to be resolved and the variable that
                // will represent the object under test.
                ExpressionParser expressionParser = new SpelExpressionParser();
                Expression expression = expressionParser
                        .parseExpression("#assertThat(#objectToTest)." + expect.getExpectation());

                // Retrieve the typed object from the JsonPath and fail if the type doesn't
                // match
                var testSubject = this.objectMapper.convertValue(objectUnderTest.get(field), expect.getType());

                // Skip this if test subject doesn't exist, this will be caught in previous
                // assertion
                if (testSubject != null) {
                    // Create the evaluation context and set the variable and function
                    StandardEvaluationContext context = new StandardEvaluationContext();
                    context.setVariable("objectToTest", testSubject);

                    // The assertThat functiion has to be reflected because of type erasure
                    // otherwise we would only be able to assert against objects
                    Method assertThatMethod = MethodUtils.getMatchingAccessibleMethod(Assertions.class, "assertThat",
                            testSubject.getClass());
                    if (assertThatMethod == null) {
                        softly.fail(
                                "Unable to verify expectation. The org.assertj.core.api.Assertions class contains no matching assertThat method for the type %s",
                                testSubject.getClass());
                    }
                    context.registerFunction("assertThat", assertThatMethod);

                    // Execute the expression and capture any EvaluationException to determine
                    // how the expectation failed
                    expression.getValue(context);
                }
            } catch (IllegalArgumentException ex) {
                softly.fail("Expected value to be of type '%s'", expect.getType());
            } catch (SpelParseException ex) {
                softly.fail("Invalid expectation: " + expect.getExpectation());
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
        });
    }

    @Autowired
    public StepDefinitions(@NonNull PersonaManager personaManager, @NonNull HttpResponseManager httpResponseManager,
            @NonNull JpaRestApiClient jpaRestApiClient, @NonNull ObjectMapper objectMapper,
            @NonNull ScenarioState scenarioState) {
        this.personaManager = personaManager;
        this.httpResponseManager = httpResponseManager;
        this.jpaRestApiClient = jpaRestApiClient;
        this.objectMapper = objectMapper;
        this.scenarioState = scenarioState;

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
     * Assert expectations against the last response
     * 
     * @param expectations A table of expectations to assert
     */
    @Then("the last response should contain")
    public void the_last_response_should_contain(List<Expectation> expectations) {
        var root = this.httpResponseManager.getLastResponse().getBody().jsonPath();
        objectMeetsExpectations(root, expectations);
    }

    /**
     *
     * Checks that a resource contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {object_to_test} should contain the fields")
    public void the_object_should_contain_fields(JsonPath objectUnderTest, List<String> fields) {
        objectContainsFields(objectUnderTest.getMap(""), fields);
    }

    /**
     *
     * Checks that a resource does not contains the given fields
     *
     * @param objectUnderTest The object to test
     * @param fields          The fields to check the response for
     */
    @Then("the {object_to_test} should not contain the fields")
    public void the_object_should_not_contain_fields(JsonPath objectUnderTest, List<String> fields) {
        objectDoesNotContainFields(objectUnderTest.getMap(""), fields);
    }

    /**
     * 
     * Retrieves a specific resource from a list of resources from the
     * specified response and applies the given expectations to that resource.
     * 
     * @param objectUnderTest The object to test
     * @param expectations    A table of expectations to assert
     */
    @Then("the {object_to_test} should contain")
    public void the_object_should_contain(JsonPath objectUnderTest, List<Expectation> expectations) {
        objectMeetsExpectations(objectUnderTest, expectations);
    }

    /**
     * 
     * Compares 2 resources to see if they are equal
     * 
     * @param objectToCompare     The object to be compared
     * @param objectToCompareWith The object to compare with
     */
    @Then("the {object_to_test} should equal the {object_to_test}")
    public void the_object_should_contain(JsonPath objectToCompare, JsonPath objectToCompareWith) {
        assertThat(objectToCompare.getMap("")).isEqualTo(objectToCompareWith.getMap(""));
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
    @Then("{each_of_the_objects_to_test} should contain")
    public void the_objects_should_contain(JsonPath objectsUnderTest, List<Expectation> expectations) {

        SoftAssertions softly = new SoftAssertions();

        var itemsSize = objectsUnderTest.getInt("size()");
        for (int i = 0; i < itemsSize; i++) {
            objectsUnderTest.setRootPath("items[" + i + "]");
            objectMeetsExpectations(softly, objectsUnderTest, expectations);
        }

        softly.assertAll();
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
    @ParameterType(FROM_IN_SERVICE)
    public String service(String name) {
        return this.scenarioState.trackService(name);
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
     * @return JsonPath
     */
    @ParameterType("(?:last|(?:(\\d+)(?:st|nd|rd|th))) of the (\\S*) in the (?:last|(?:(\\d+)(?:st|nd|rd|th))) (?:\\\"([^\\\"]*)\\\" )?response"
            + FROM_IN_SERVICE)
    public JsonPath object_to_test(String objectPosition,
            String resourceName, String responsePosition, String path, String service) {

        String targetService = this.scenarioState.trackService(service);

        URL url;
        if (path == null || path.isEmpty()) {
            url = this.jpaRestApiClient.GetResourceURL(targetService, resourceName);
        } else {
            url = this.jpaRestApiClient.GetServiceURL(targetService, path);
        }

        int responseIndex = getIndex(responsePosition);
        Response response = this.httpResponseManager.getResponse(url, responseIndex);
        var itemsPath = response.getBody().jsonPath().setRootPath("items");
        int objectIndex = getIndex(objectPosition);
        if (objectIndex == -1) {
            objectIndex = itemsPath.getInt("size()") - 1;
        }

        return itemsPath.setRootPath("items[" + objectIndex + "]");
    }

    /**
     * 
     * Extracts all resources from a specific response
     * 
     * @param resourceName     The name of the type of resource to extract
     * @param responsePosition The ordinal of the response to retrieve the resource
     *                         from
     * @param path             The path specified in the request when retrieve
     *                         resources from a GET request (Optional)
     * @param service          The service the request was made to
     * @return JsonPath
     */
    @ParameterType("each of the (\\S*) in the (?:last|(?:(\\d+)(?:st|nd|rd|th))) (?:\\\"([^\\\"]*)\\\" )?response"
            + FROM_IN_SERVICE)
    public JsonPath each_of_the_objects_to_test(String resourceName, String responsePosition, String path,
            String service) {

        String targetService = this.scenarioState.trackService(service);

        URL url;
        if (path == null || path.isEmpty()) {
            url = this.jpaRestApiClient.GetResourceURL(targetService, resourceName);
        } else {
            url = this.jpaRestApiClient.GetServiceURL(targetService, path);
        }

        int responseIndex = getIndex(responsePosition);
        Response response = this.httpResponseManager.getResponse(url, responseIndex);
        var itemsPath = response.getBody().jsonPath().setRootPath("items");
        return itemsPath;
    }

    /**
     * 
     * DataTable conversion for expectations. Converts tables
     * with the columns field, type, and expectation.
     * The field is a property name on the object and the
     * type defines the class of the field. The type can be
     * a fully qualified name or be a short version added to
     * {@see JpaTestContext#classSimpleStrings}
     * 
     * @param entry The table row to be converted
     * @return Expectation
     */
    @DataTableType
    public Expectation expectationEntry(Map<String, String> entry) {
        String type = entry.get("type");
        Objects.requireNonNull(type, "A type must be specified for the expectation");

        Class<?> clazz = resolveType(type);

        Expectation expectation = null;
        try {
            expectation = new Expectation(
                    entry.get("field"),
                    clazz,
                    entry.get("expectation"));
        } catch (NullPointerException exx) {
            fail("Expectation tables are expected to contain the fields 'field', 'type', and 'expectation'. Each field requires a valid value");
        }

        return expectation;
    }

    /**
     * 
     * Resolves a specified type. If a simple name is used
     * it is first looked up in {@see JpaTestContext#classSimpleStrings}
     * Otherwise it will be resolved using {@see Class#forName(String)}
     * If neither method returns a result an attempt is made to
     * resolve the class from the {@see ObjectMapper#getTypeFactory()}
     * 
     * @param type The name of the class to find
     * @return Class<?>
     */
    private Class<?> resolveType(String type) {

        Class<?> clazz = null;
        if (type.contains(".")) {
            try {
                clazz = Class.forName(type);
            } catch (ClassNotFoundException e) {
            }
        } else {
            clazz = JpaTestContext.classSimpleStrings.get(type);
        }

        if (clazz == null) {
            try {
                clazz = this.objectMapper.getTypeFactory().findClass(type);
            } catch (ClassNotFoundException e) {
                fail("Unknown type '%s'. To configure use JpaTestContext.put(\"%<s\", FullyQualifiedTypeName.class);",
                        type);
            }
        }

        return clazz;

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
        return Integer.parseInt(responsePosition) - 1;
    }

}
