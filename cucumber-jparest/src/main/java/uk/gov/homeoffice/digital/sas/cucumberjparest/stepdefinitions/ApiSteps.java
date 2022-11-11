package uk.gov.homeoffice.digital.sas.cucumberjparest.stepdefinitions;

import io.cucumber.java.en.When;
import java.util.Map;
import lombok.NonNull;
import uk.gov.homeoffice.digital.sas.cucumberjparest.HttpResponseManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiClient;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiResourceResponse;
import uk.gov.homeoffice.digital.sas.cucumberjparest.JpaRestApiResponse;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PayloadManager;
import uk.gov.homeoffice.digital.sas.cucumberjparest.PayloadManager.PayloadKey;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Persona;
import uk.gov.homeoffice.digital.sas.cucumberjparest.Resource;

/**
 * Provides steps related to making API requests to endpoints exposed by the JpaRest package.
 */
public class ApiSteps {

  private final HttpResponseManager httpResponseManager;
  private final JpaRestApiClient jpaRestApiClient;
  private final PayloadManager payloadManager;

  public ApiSteps(@NonNull HttpResponseManager httpResponseManager,
      @NonNull JpaRestApiClient jpaRestApiClient,
      PayloadManager payloadManager) {
    this.httpResponseManager = httpResponseManager;
    this.jpaRestApiClient = jpaRestApiClient;
    this.payloadManager = payloadManager;

  }

  /**
   * Posts the referenced payload to the endpoint exposed for the given resource.
   *
   * @param persona    The persona to use for auth context
   * @param payloadKey The key for the payload (resource type and name)
   * @param service    The service to use
   */
  @When("{persona} creates {payload}{service}")
  public void personaCreatesResourceFromPayloadInTheService(Persona persona,
      PayloadKey payloadKey,
      String service) {

    String payload = this.payloadManager.getPayload(payloadKey);
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.create(persona, service,
        payloadKey.getResourceType(),
        payload);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }

  /**
   * Retrieves the specified resource type from the given service.
   *
   * @param persona  The persona to use for auth context
   * @param resource The type of resources to be retrieved
   * @param service  There service to use
   */
  @When("{persona} retrieves {word}{service}")
  public void personaRetrievesResourcesFromTheService(Persona persona, String resource,
      String service) {
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.retrieve(persona, service,
        resource, null);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }

  /**
   * Retrieves the specified resource type from the given service.
   *
   * @param persona  The persona to use for auth context
   * @param resource The type of resources to be retrieved
   * @param service  There service to use
   */
  @When("{persona} retrieves {word}{service} with")
  public void personaRetrievesResourcesFromTheService(Persona persona, String resource,
      String service,
      Map<String, String> parameters) {
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.retrieve(persona, service,
        resource, parameters);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }

  /**
   * Makes a GET request to the service with the given path.
   *
   * @param persona The persona to use for auth context
   * @param path    The URL to request
   * @param service There service to use
   */
  @When("{persona} GETs {string}{service}")
  public void personaGetsUrlFromService(Persona persona, String path, String service) {
    JpaRestApiResponse apiResponse = this.jpaRestApiClient.get(persona, service, path);

    this.httpResponseManager.addResponse(apiResponse.getUri(), apiResponse.getResponse());
  }

  /**
   * Retrieves the specified resource by identifier from the given service.
   *
   * @param persona    The persona to use for auth context
   * @param resource   The type of resources to be retrieved
   * @param identifier The resource identifier
   * @param service    There service to use
   */
  @When("{persona} gets the {word} {string}{service}")
  public void personaRetrieveSpecificResourcesFromTheService(Persona persona,
      String resource,
      String identifier, String service) {
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.retrieveById(persona, service,
        resource,
        identifier);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }

  /**
   * Deletes the specified resource from the given service.
   *
   * @param persona  The persona to use for auth context
   * @param resource The resource to be deleted
   * @param service  There service to use
   */
  @When("{persona} deletes the {resource}{service}")
  public void personaDeletesTheResource(Persona persona, Resource resource, String service) {
    String reference = resource.getJsonPath().getString("id");
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.delete(persona, service,
        resource.getResourceType(), reference);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }

  /**
   * Updates the specified resource in the given service.
   *
   * @param persona    The persona to use for auth context
   * @param resource   The resource to be updated
   * @param service    There service to use
   * @param payloadKey The key for the payload (resource type and name)
   */
  @When("{persona} updates the {resource}{service} with {payload}")
  public void personaUpdatesTheResource(Persona persona, Resource resource, String service,
      PayloadKey payloadKey) {

    String reference = resource.getJsonPath().getString("id");
    String payload = this.payloadManager.getPayload(payloadKey);
    JpaRestApiResourceResponse apiResponse = this.jpaRestApiClient.update(persona, service,
        payloadKey.getResourceType(), reference,
        payload);

    this.httpResponseManager.addResponse(apiResponse.getBaseResourceUri(),
        apiResponse.getResponse());
  }
}
