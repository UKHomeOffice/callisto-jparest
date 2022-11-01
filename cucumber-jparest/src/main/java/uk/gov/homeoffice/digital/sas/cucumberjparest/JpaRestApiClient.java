package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;
import static uk.gov.homeoffice.digital.sas.jparest.controller.enums.RequestParameter.TENANT_ID;

import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The purpose of this class is to understand the protocol implemented by the JpaRest library. At
 * this moment in time it knows how to perform CRUD operations on the endpoints exposed but when
 * authorisation is implemented in the API this class will be updated to add authentication to the
 * requests being made.
 */
@Component
public class JpaRestApiClient {

  public static final String API_ROOT_PATH = "/resources/";
  public static final String TENANT_ID_SYSTEM_PROPERTY_NAME = "cucumber.jparest.tenantId";
  private static final String TENANT_ID_SYSTEM_PROPERTY_EL
      = "#{systemProperties['" + TENANT_ID_SYSTEM_PROPERTY_NAME + "']}";

  /**
   * Injecting tenantId parameter as a system property to avoid verbosity in BDD features. This is a
   * temporary workaround, as in the long term, tenantId would be part of the authenticated user
   * object and would not have to be supplied as a request parameter
   */
  @Value(TENANT_ID_SYSTEM_PROPERTY_EL)
  private String tenantId;

  /**
   * @param requestSpecification Request Specification
   * @param persona Persona
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
   * Retrieves the full URL called as a result of the specification
   *
   * @param requestSpecification The specification to retrieve the URL from
   * @return URL
   */
  private static URL getUrl(@NonNull RequestSpecification requestSpecification) {
    QueryableRequestSpecification queryRequest = SpecificationQuerier.query(requestSpecification);

    String absoluteUri = queryRequest.getURI();
    URL requestUrl = null;
    try {
      requestUrl = (new URI(absoluteUri)).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      fail(e.getMessage());
    }

    return requestUrl;
  }

  @Getter
  private final ServiceRegistry serviceRegistry;

  @Autowired
  public JpaRestApiClient(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = Objects.requireNonNull(serviceRegistry,
        "serviceRegistry must not be null");
  }

  /**
   * Creates resources in the specified service using the provided payload.
   *
   * @param persona  The persona making the request.
   * @param service  The name of the service where the resources will be created. The service name
   *                 must exist in the {@link ServiceRegistry}
   * @param resource The name of the type of resource to create
   * @param payload  The resource to create
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse create(Persona persona, String service, String resource,
      String payload) {
    URL url = getResourceUrl(service, resource);
    RequestSpecification spec = given()
        .baseUri(url.toString())
        .body(payload)
        .queryParam(TENANT_ID.getParamName(), tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    URL requestUrl = getUrl(spec);
    Response response = spec.post();

    return new JpaRestApiResourceResponse(url, requestUrl, response);
  }

  /**
   * Retrieves resources in the specified service using the provided filter.
   *
   * @param persona    The persona making the request.
   * @param service    The name of the service where the resources will be retrieved from. The
   *                   service name must exist in the {@link ServiceRegistry}
   * @param resource   The name of the type of resource to be retrieved
   * @param parameters The query string parameters to add to the requested url
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse retrieve(Persona persona, String service, String resource,
      Map<String, String> parameters) {
    URL url = getResourceUrl(service, resource);

    RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam(TENANT_ID.getParamName(), tenantId);

    if (parameters != null) {
      spec = spec.queryParams(parameters);
    }

    addPersonaAuthToRequestSpecification(spec, persona);

    URL requestUrl = getUrl(spec);
    Response response = spec.get();

    return new JpaRestApiResourceResponse(url, requestUrl, response);

  }

  /**
   * Retrieves resources in the specified service using the specified reference.
   *
   * @param persona   The persona making the request.
   * @param service   The name of the service where the resource will be retrieved from. The service
   *                  name must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to be retrieved
   * @param reference The identifier of the resource to be retrieved
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse retrieveById(Persona persona, String service, String resource,
      String reference) {
    URL url = getResourceUrl(service, resource);

    RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam(TENANT_ID.getParamName(), tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URL requestUrl = getUrl(spec);
    Response response = spec.get();

    return new JpaRestApiResourceResponse(url, requestUrl, response);

  }

  /**
   * Updates resources in the specified service using the provided payload.
   *
   * @param persona   The persona making the request.
   * @param service   The name of the service where the resources will be updated. The service name
   *                  must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to update
   * @param reference The identifier of the resource to be updated
   * @param payload   The updated resource
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse update(Persona persona, String service, String resource,
      String reference,
      String payload) {

    URL url = getResourceUrl(service, resource);
    RequestSpecification spec = given()
        .baseUri(url.toString())
        .body(payload)
        .queryParam(TENANT_ID.getParamName(), tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URL requestUrl = getUrl(spec);
    Response response = spec.put();

    return new JpaRestApiResourceResponse(url, requestUrl, response);
  }

  /**
   * Deletes resources in the specified service with the specified reference.
   *
   * @param persona   The persona making the request.
   * @param service   The name of the service where the resources will be deleted. The service name
   *                  must exist in the {@link ServiceRegistry}
   * @param resource  The name of the type of resource to deleted
   * @param reference The identifier of the resource to be deleted
   * @return JpaRestApiResourceResponse
   */
  public JpaRestApiResourceResponse delete(Persona persona, String service, String resource,
      String reference) {
    URL url = getResourceUrl(service, resource);

    RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam(TENANT_ID.getParamName(), tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    spec.basePath(reference);
    URL requestUrl = getUrl(spec);
    Response response = spec.delete();

    return new JpaRestApiResourceResponse(url, requestUrl, response);
  }

  /**
   * Retrieves resources in the specified service using the provided filter.
   *
   * @param persona The persona making the request.
   * @param service The name of the service where the resources will be retrieved from. The service
   *                name must exist in the {@link ServiceRegistry}
   * @return JpaRestApiResponse
   */
  public JpaRestApiResponse get(Persona persona, String service, String path) {
    URL url = getServiceUrl(service, path);

    RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam(TENANT_ID.getParamName(), tenantId);

    addPersonaAuthToRequestSpecification(spec, persona);

    Response response = spec.get();

    return new JpaRestApiResponse(url, response);
  }

  /**
   * Gets the base path to a resource endpoint
   *
   * @param service  The service hosting the resources
   * @param resource The resource to return the URL for
   * @return URL
   */
  public URL getResourceUrl(String service, String resource) {

    String baseUrl = this.serviceRegistry.getService(service);
    String url = baseUrl + API_ROOT_PATH + resource;

    URL resourceUrl = null;
    try {
      resourceUrl = new URL(url);
    } catch (MalformedURLException e) {
      fail(e.getMessage());
    }

    return resourceUrl;
  }

  /**
   * Gets the URL for a service for the given relative path
   *
   * @param service The target service
   * @param path    The relative URL
   * @return URL
   */
  public URL getServiceUrl(String service, String path) {

    String baseUrl = this.serviceRegistry.getService(service);
    String url = baseUrl + path;

    URL resourceUrl = null;
    try {
      resourceUrl = new URL(url);
    } catch (MalformedURLException e) {
      fail(e.getMessage());
    }

    return resourceUrl;
  }
}