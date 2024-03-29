package uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.parametertypes;

import static org.assertj.core.api.Fail.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import org.assertj.core.util.Files;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.PayloadManager.PayloadKey;
import uk.gov.homeoffice.digital.sas.cucumberjparest.api.Resource;
import uk.gov.homeoffice.digital.sas.cucumberjparest.config.JpaTestContext;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.PersonaManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.ScenarioState;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.expectations.FieldExpectation;
import uk.gov.homeoffice.digital.sas.cucumberjparest.scenarios.interpolation.Interpolation;

/**
 * Common parameter types used to convert expressions into the types required by the step
 * definitions.
 */
@SuppressWarnings("squid:S5960")// Assertions are needed in this test library
public class ParameterTypes {

  private static final String FROM_IN_SERVICE = "(?: (?:from|in) the (\\S*) service)?";

  private final Logger logger = Logger.getLogger(ParameterTypes.class.getName());

  private final PersonaManager personaManager;
  private final HttpResponseManager httpResponseManager;
  private final JpaRestApiClient jpaRestApiClient;
  private final ScenarioState scenarioState;
  private final ObjectMapper objectMapper;
  private final Interpolation interpolation;

  public ParameterTypes(@NonNull PersonaManager personaManager,
      @NonNull HttpResponseManager httpResponseManager,
      @NonNull JpaRestApiClient jpaRestApiClient, @NonNull ObjectMapper objectMapper,
      @NonNull ScenarioState scenarioState, Interpolation interpolation) {
    this.personaManager = personaManager;
    this.httpResponseManager = httpResponseManager;
    this.jpaRestApiClient = jpaRestApiClient;
    this.objectMapper = objectMapper;
    this.scenarioState = scenarioState;
    this.interpolation = interpolation;
  }

  /**
   * Matches a "{variable name} {resourceType}" and creates a PayloadKey for retrieving payloads
   * from the {@link PayloadManager}.
   *
   * @param payloadName  The variable used to reference the resource
   * @param resourceType The type of resource represented
   * @return PayloadKey
   */
  @ParameterType("(?:the )?(\\w+) ([\\w-]+)")
  public PayloadKey payload(String payloadName, String resourceType) {
    return new PayloadKey(resourceType, payloadName);
  }

  /**
   * Matches when a file is specified and returns the contents of the specified file.
   *
   * @param path The path of the file
   * @return String The contents of the file
   */
  @ParameterType("file '([^']*)'")
  public String fileContents(String path) {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      String decodedPath = URLDecoder.decode(classLoader.getResource(path).getFile(), "UTF-8");
      File file = new File(decodedPath);
      String data = Files.contentOf(file, "UTF-8");
      return interpolation.evaluate(data);
    } catch (RuntimeException | UnsupportedEncodingException ex) {
      throw new IllegalArgumentException("File doesn't exist " + path);
    }
  }

  /**
   * Gets the persona for the given name.
   *
   * @param name The name of the persona
   * @return Persona The persona associated with the given name
   */
  @ParameterType("(?:the )?(\\S*)")
  public Persona persona(String name) {
    return this.personaManager.getPersona(name);
  }

  /**
   * Retrieves the location of the given service.
   *
   * @param name The name of the service
   * @return String The URL of the service
   */
  @ParameterType(FROM_IN_SERVICE)
  public String service(String name) {
    return this.scenarioState.trackService(name);
  }

  private JsonPath getItemsPath(String resourceName, String responsePosition, String path,
      String service) {
    String targetService = this.scenarioState.trackService(service);

    URI uri;
    if (path == null || path.isEmpty()) {
      uri = this.jpaRestApiClient.getResourceUri(targetService, resourceName);
    } else {
      uri = this.jpaRestApiClient.getServiceUrl(targetService, path);
    }

    int responseIndex = getIndex(responsePosition);
    Response response = this.httpResponseManager.getResponse(uri, responseIndex);
    return response.getBody().jsonPath().setRootPath("items");
  }

  /**
   * Extracts a specific object from a specific response.
   *
   * @param objectPosition   The position of the resource to return
   * @param resourceName     The name of the type of resource to extract
   * @param responsePosition The ordinal of the response to retrieve the resource from
   * @param path             The path specified in the request when retrieve resources from a GET
   *                         request (Optional)
   * @param service          The service the request was made to
   * @return Resource
   */
  @ParameterType(
      "(?:last|(?:(\\d+)(?:st|nd|rd|th))) of the (\\S*) "
          + "in the (?:last|(?:(\\d+)(?:st|nd|rd|th))) (?:\\\"([^\\\"]*)\\\" )?response"
          + FROM_IN_SERVICE)
  public Resource resource(String objectPosition,
      String resourceName, String responsePosition, String path, String service) {

    var itemsPath = getItemsPath(resourceName, responsePosition, path, service);
    int objectIndex = getIndex(objectPosition);
    if (objectIndex == -1) {
      objectIndex = itemsPath.getInt("size()") - 1;
    }

    return new Resource(resourceName, itemsPath.setRootPath("items[" + objectIndex + "]"));
  }

  /**
   * Extracts all resources from a specific response.
   *
   * @param resourceName     The name of the type of resource to extract
   * @param responsePosition The ordinal of the response to retrieve the resource from
   * @param path             The path specified in the request when retrieve resources from a GET
   *                         request (Optional)
   * @param service          The service the request was made to
   * @return Resource
   */
  @ParameterType(
      "each of the (\\S*) in the (?:last|(?:(\\d+)(?:st|nd|rd|th))) "
          + "(?:\\\"([^\\\"]*)\\\" )?response"
          + FROM_IN_SERVICE)
  public Resource eachResource(String resourceName, String responsePosition, String path,
      String service) {

    var itemsPath = getItemsPath(resourceName, responsePosition, path, service);
    return new Resource(resourceName, itemsPath);
  }

  /**
   * DataTable conversion for expectations. It converts tables with the columns field, type, and
   * expectation. The field is a property name on the object and the type defines the class of the
   * field. The type can be a fully qualified name or be a short version added to
   * {@link JpaTestContext#classSimpleStrings}
   *
   * @param entry The table row to be converted
   * @return Expectation
   */
  @DataTableType
  public FieldExpectation expectationEntry(Map<String, String> entry) {
    String type = entry.get("type");
    Objects.requireNonNull(type, "A type must be specified for the expectation");

    Class<?> clazz = resolveType(type);

    FieldExpectation fieldExpectation = null;
    try {
      fieldExpectation = new FieldExpectation(
          entry.get("field"),
          clazz,
          entry.get("expectation"));
    } catch (NullPointerException exx) {
      fail(
          "Expectation tables are expected to contain the fields 'field', 'type', "
              + "and 'expectation'. Each field requires a valid value");
    }

    return fieldExpectation;
  }

  /**
   * Resolves a specified type. If a simple name is used it is first looked up in
   * {@link JpaTestContext#classSimpleStrings}, otherwise it will be resolved using
   * {@link Class#forName(String)}. If neither method returns a result an attempt is made to resolve
   * the class from the {@link  ObjectMapper#getTypeFactory()}
   *
   * @param type The name of the class to find
   * @return Class<?>
   */
  Class<?> resolveType(String type) {

    Class<?> clazz = null;
    if (type.contains(".")) {
      try {
        clazz = Class.forName(type);
      } catch (ClassNotFoundException e) {
        logger.log(Level.SEVERE, "Could not resolve type: {0}", type);
      }
    } else {
      clazz = JpaTestContext.classSimpleStrings.get(type);
    }

    if (clazz == null) {
      try {
        clazz = this.objectMapper.getTypeFactory().findClass(type);
      } catch (ClassNotFoundException e) {
        fail(
            "Unknown type '%s'. To configure use "
                + "JpaTestContext.put(\"%<s\", FullyQualifiedTypeName.class);",
            type);
      }
    }

    return clazz;

  }

  /**
   * Converts positional string last, 1st, 2nd, 23rd, 30th etc to a zero based ordinal integer. The
   * word last is converted to a -1
   *
   * @param responsePosition response position
   * @return int
   */
  private int getIndex(String responsePosition) {
    if (responsePosition == null) {
      return -1;
    }
    return Integer.parseInt(responsePosition) - 1;
  }

}
