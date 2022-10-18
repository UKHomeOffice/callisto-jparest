package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import lombok.Getter;
import lombok.NonNull;

/**
 * The purpose of this class is to understand the protocol
 * implemented by the JpaRest library.
 * At this moment in time it knows how to perform CRUD
 * operations on the endpoints exposed but when authorisation
 * is implemented in the API this class will be updated to
 * add authentication to the requests being made.
 */
@Component
public class JpaRestApiClient {

    public static final String API_ROOT_PATH = "/resources/";

    /**
     * @param requestSpecification
     * @param persona
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
     * 
     * Retrieves the full URL called as a result of the specification
     * 
     * @param requestSpecification The specification to retrieve the URL from
     * @return URL
     */
    private static URL getURL(@NonNull RequestSpecification requestSpecification) {
        QueryableRequestSpecification queryRequest = SpecificationQuerier.query(requestSpecification);

        String absoluteURI = queryRequest.getURI();
        URL requestURL = null;
        try {
            requestURL = (new URI(absoluteURI)).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            fail(e.getMessage());
        }

        return requestURL;
    }

    @Getter
    private final ServiceRegistry serviceRegistry;

    @Autowired
    public JpaRestApiClient(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry must not be null");
    }

    /**
     * 
     * Creates resources in the specified service using the
     * provided payload.
     * 
     * @param persona  The persona making the request.
     * @param service  The name of the service where the resources will be created.
     *                 The service name must exist in the {@link ServiceRegistry}
     * @param resource The name of the type of resource to create
     * @param payload  The resource to create
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Create(Persona persona, String service, String resource, String payload) {
        URL url = GetResourceURL(service, resource);
        RequestSpecification spec = given()
                .baseUri(url.toString())
                .body(payload)
                .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        URL requestURL = getURL(spec);
        Response response = spec.post();

        return new JpaRestApiResourceResponse(url, requestURL, response);
    }

    /**
     * 
     * Retrieves resources in the specified service using the
     * provided filter.
     * 
     * @param persona    The persona making the request.
     * @param service    The name of the service where the resources will be
     *                   retrieved
     *                   from.
     *                   The service name must exist in the {@link ServiceRegistry}
     * @param resource   The name of the type of resource to be retrieved
     * @param parameters The query string parameters to add to the requested url
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Retrieve(Persona persona, String service, String resource,
            Map<String, String> parameters) {
        URL url = GetResourceURL(service, resource);

        RequestSpecification spec = given()
                .baseUri(url.toString());

        if (parameters != null) {
            spec = spec.queryParams(parameters);
        }

        addPersonaAuthToRequestSpecification(spec, persona);

        URL requestURL = getURL(spec);
        Response response = spec.get();

        return new JpaRestApiResourceResponse(url, requestURL, response);

    }

    /**
     * 
     * Retrieves resources in the specified service using the
     * specified reference.
     * 
     * @param persona   The persona making the request.
     * @param service   The name of the service where the resource will be retrieved
     *                  from.
     *                  The service name must exist in the {@link ServiceRegistry}
     * @param resource  The name of the type of resource to be retrieved
     * @param reference The identifier of the resource to be retrieved
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse RetrieveById(Persona persona, String service, String resource, String reference) {
        URL url = GetResourceURL(service, resource);

        RequestSpecification spec = given()
                .baseUri(url.toString())
                .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        spec.basePath(reference);
        URL requestURL = getURL(spec);
        Response response = spec.get();

        return new JpaRestApiResourceResponse(url, requestURL, response);

    }

    /**
     * 
     * Updates resources in the specified service using the
     * provided payload.
     * 
     * @param persona   The persona making the request.
     * @param service   The name of the service where the resources will be updated.
     *                  The service name must exist in the {@link ServiceRegistry}
     * @param resource  The name of the type of resource to update
     * @param reference The identifier of the resource to be updated
     * @param payload   The updated resource
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Update(Persona persona, String service, String resource, String reference,
            String payload) {

        URL url = GetResourceURL(service, resource);
        RequestSpecification spec = given()
                .baseUri(url.toString())
                .body(payload)
                .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        spec.basePath(reference);
        URL requestURL = getURL(spec);
        Response response = spec.put();

        return new JpaRestApiResourceResponse(url, requestURL, response);
    }

    /**
     * 
     * Deletes resources in the specified service with the
     * specified reference.
     * 
     * @param persona   The persona making the request.
     * @param service   The name of the service where the resources will be deleted.
     *                  The service name must exist in the {@link ServiceRegistry}
     * @param resource  The name of the type of resource to deleted
     * @param reference The identifier of the resource to be deleted
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Delete(Persona persona, String service, String resource, String reference) {
        URL url = GetResourceURL(service, resource);

        RequestSpecification spec = given()
                .baseUri(url.toString())
                .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        spec.basePath(reference);
        URL requestURL = getURL(spec);
        Response response = spec.delete();

        return new JpaRestApiResourceResponse(url, requestURL, response);
    }

    /**
     * 
     * Retrieves resources in the specified service using the
     * provided filter.
     * 
     * @param persona  The persona making the request.
     * @param service  The name of the service where the resources will be retrieved
     *                 from.
     *                 The service name must exist in the {@link ServiceRegistry}
     * @return JpaRestApiResponse
     */
    public JpaRestApiResponse Get(Persona persona, String service, String path) {
        URL url = GetServiceURL(service, path);

        RequestSpecification spec = given()
                .baseUri(url.toString())
                .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        Response response = spec.get();

        return new JpaRestApiResponse(url, response);
    }

    /**
     * 
     * Gets the base path to a resource endpoint
     * 
     * @param service  The service hosting the resources
     * @param resource The resource to return the URL for
     * @return URL
     */
    public URL GetResourceURL(String service, String resource) {

        String baseUrl = this.serviceRegistry.getService(service);
        String url = baseUrl + API_ROOT_PATH + resource;

        URL resourceURL = null;
        try {
            resourceURL = new URL(url);
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }

        return resourceURL;
    }

    /**
     * 
     * Gets the URL for a service for the given relative path
     * 
     * @param service The target service
     * @param path    The relative URL
     * @return URL
     */
    public URL GetServiceURL(String service, String path) {

        String baseUrl = this.serviceRegistry.getService(service);
        String url = baseUrl + path;

        URL resourceURL = null;
        try {
            resourceURL = new URL(url);
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }

        return resourceURL;
    }
}