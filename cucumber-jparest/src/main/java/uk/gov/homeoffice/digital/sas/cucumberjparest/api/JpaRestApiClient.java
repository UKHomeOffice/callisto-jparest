package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;

import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.digital.sas.cucumberjparest.persona.Persona;

/**
 * The purpose of this class is to understand the protocol implemented by the JpaRest library. At
 * this moment in time it knows how to perform CRUD operations on the endpoints exposed but when
 * authorisation is implemented in the API this class will be updated to add authentication to the
 * requests being made.
 */
@Component
public class JpaRestApiClient {

  @SuppressWarnings("squid:S1075") // URIs should not be hardcoded
  public static final String API_ROOT_PATH = "/resources/";
  public static final String TENANT_ID_PARAM_NAME = "tenantId";
  public static final String TENANT_ID_SYSTEM_PROPERTY_NAME = "cucumber.jparest.tenantId";
  @Getter
  private final ServiceRegistry serviceRegistry;

  @Autowired
  public JpaRestApiClient(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = Objects.requireNonNull(serviceRegistry,
        "serviceRegistry must not be null");
  }

  /**
   * @param requestSpecification Request Specification
   * @param persona              Persona
   * @return RequestSpecification
   */
  private static RequestSpecification addPersonaAuthToRequestSpecification(
      @NonNull RequestSpecification requestSpecification, @NonNull Persona persona) {
    String auth = persona.getAuthToken();
    if (auth != null && !auth.isEmpty()) {
      requestSpecification.header(HttpHeaders.AUTHORIZATION, auth);
    }
    return requestSpecification;
  }

  /**
   * Retrieves the full URI called as a result of the specification.
   *
   * @param requestSpecification The specification to retrieve the URL from
   * @return URI
   */
  @SuppressWarnings("squid:S5960")// Assertions are needed in this test library
  private static URI getUri(@NonNull RequestSpecification requestSpecification) {
    QueryableRequestSpecification queryRequest = SpecificationQuerier.query(requestSpecification);

    String absoluteUri = queryRequest.getURI();
    URI requestUri = null;
    try {
      requestUri = new URI(absoluteUri);
    } catch (URISyntaxException e) {
      fail(e.getMessage());
    }

    return requestUri;
  }

  /**
   * Creates resources in the specified service using the provided payload.
   *
   * @param persona  The persona making the request.
   * @param tenantId The tenant ID; if not supplied fallback on system property
   * @param service  The name of the service where the resources will be created. The service name
   *                 must exist in the {@link ServiceRegistry}
   * @param resource The name of the type of resource to create
   * @param payload  The resource to create
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse create(Persona persona, String tenantId, String service,
      String resource, String payload) {
    URI uri = getResourceUri(service, resource);
    RequestSpecification spec = given()
        .baseUri(uri.toString())
        .body(payload)
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    URI requestUri = getUri(spec);
    Response response = spec.post();

    return new JpaRestApiResourceResponse(uri, requestUri, response);
  }

  /**
   * Retrieves resources in the specified service using the provided filter.
   *
   * @param persona    The persona making the request.
   * @param tenantId   The tenant ID; if not supplied fallback on system property
   * @param service    The name of the service where the resources will be retrieved from. The
   *                   service name must exist in the {@link ServiceRegistry}
   * @param resource   The name of the type of resource to be retrieved
   * @param parameters The query string parameters to add to the requested url
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse retrieve(Persona persona, String tenantId, String service,
      String resource, Map<String, String> parameters) {
    URI uri = getResourceUri(service, resource);

    RequestSpecification spec = given()
        .baseUri(uri.toString())
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    if (parameters != null) {
      spec = spec.queryParams(parameters);
    }

    addPersonaAuthToRequestSpecification(spec, persona);

    URI requestUri = getUri(spec);
    Response response = spec.get();

    return new JpaRestApiResourceResponse(uri, requestUri, response);

  }

  /**
   * Retrieves resources in the specified service using the specified reference.
   *
   * @param persona   The persona making the request.
   * @param tenantId  The tenant ID; if not supplied fallback on system property
   * @param service   The name of the service where the resource will be retrieved from. The service
   *                  name must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to be retrieved
   * @param reference The identifier of the resource to be retrieved
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse retrieveById(Persona persona, String tenantId, String service,
      String resource, String reference) {
    URI uri = getResourceUri(service, resource);

    RequestSpecification spec = given()
        .baseUri(uri.toString())
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URI requestUri = getUri(spec);
    Response response = spec.get();

    return new JpaRestApiResourceResponse(uri, requestUri, response);

  }

  /**
   * Updates resources in the specified service using the provided payload.
   *
   * @param persona   The persona making the request.
   * @param tenantId  The tenant ID; if not supplied fallback on system property
   * @param service   The name of the service where the resources will be updated. The service name
   *                  must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to update
   * @param reference The identifier of the resource to be updated
   * @param payload   The updated resource
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse update(Persona persona, String tenantId, String service,
      String resource, String reference, String payload) {

    URI uri = getResourceUri(service, resource);
    RequestSpecification spec = given()
        .baseUri(uri.toString())
        .body(payload)
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URI requestUri = getUri(spec);
    Response response = spec.put();

    return new JpaRestApiResourceResponse(uri, requestUri, response);
  }

  /**
   * Deletes resources in the specified service with the specified reference.
   *
   * @param persona   The persona making the request.
   * @param tenantId  The tenant ID; if not supplied fallback on system property
   * @param service   The name of the service where the resources will be deleted. The service name
   *                  must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to deleted
   * @param reference The identifier of the resource to be deleted
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse delete(Persona persona, String tenantId, String service,
      String resource, String reference) {
    URI uri = getResourceUri(service, resource);

    RequestSpecification spec = given()
        .baseUri(uri.toString())
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URI requestUri = getUri(spec);
    Response response = spec.delete();

    return new JpaRestApiResourceResponse(uri, requestUri, response);
  }

  /**
   * Makes the GET request to the specified service using the provided path. The path could be a
   * resource path or any other endpoint (e.g. openAPI)
   *
   * @param persona  The persona making the request.
   * @param tenantId The tenant ID; if not supplied fallback on system property
   * @param service  The name of the service where the resources will be retrieved from. The service
   *                 name must exist in the {@link ServiceRegistry}
   * @param path     The relative path to make the GET request to
   * @return JpaRestApiResponse
   */
  public JpaRestApiResponse get(Persona persona, String tenantId, String service, String path) {
    URI url = getServiceUrl(service, path);

    RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam(TENANT_ID_PARAM_NAME,
            tenantId == null ? System.getProperty(TENANT_ID_SYSTEM_PROPERTY_NAME) : tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    Response response = spec.get();

    return new JpaRestApiResponse(url, response);
  }

  /**
   * Gets the base path to a resource endpoint.
   *
   * @param service  The service hosting the resources
   * @param resource The resource to return the URL for
   * @return URL
   */
  public URI getResourceUri(String service, String resource) {

    String baseUri = this.serviceRegistry.getService(service);
    String uri = baseUri + API_ROOT_PATH + resource;

    URI resourceUri = null;
    try {
      resourceUri = new URI(uri);
    } catch (URISyntaxException e) {
      fail(e.getMessage());
    }

    return resourceUri;
  }

  /**
   * Gets the URL for a service for the given relative path.
   *
   * @param service The target service
   * @param path    The relative URL
   * @return URI
   */
  public URI getServiceUrl(String service, String path) {

    String baseUrl = this.serviceRegistry.getService(service);
    String url = baseUrl + path;

    URI resourceUri = null;
    try {
      resourceUri = new URI(url);
    } catch (URISyntaxException e) {
      fail(e.getMessage());
    }

    return resourceUri;
  }
}