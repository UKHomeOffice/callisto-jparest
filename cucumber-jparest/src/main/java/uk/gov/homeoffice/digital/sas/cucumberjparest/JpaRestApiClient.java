package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import org.apache.http.HttpHeaders;
import org.assertj.core.api.Fail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.restassured.response.Response;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;

import lombok.Getter;
import lombok.NonNull;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Fail.fail;
/**
 * The purpose of this class is to understand the protocol
 * implemented by the JpaRest library.
 * 
 * At this moment in time it knows how to perform CRUD
 * operations on the endpoints exposed but when authorisation
 * is implemented in the API this class will be updated to
 * add authentication to the requests being made.
 */
@Component
public class JpaRestApiClient {

    public static final String API_ROOT_PATH = "/resources/";
    
    private static RequestSpecification addPersonaAuthToRequestSpecification(@NonNull RequestSpecification requestSpecification, @NonNull Persona persona) {
        String auth = persona.getAuthToken();
        if (auth != null && !auth.isEmpty()) {
            requestSpecification.header(HttpHeaders.AUTHORIZATION, auth);
        }
        return requestSpecification;
    }

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
        this.serviceRegistry = Objects.requireNonNull( serviceRegistry, "serviceRegistry must not be null" );
    }

    /** 
     * 
     * Creates resources in the specified service using the 
     * provided payload.
     * 
     * @param persona The persona making the request.
     * @param service The name of the service where the resources will be created.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to create
     * @param payload The resource to create
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
     * @param persona The persona making the request.
     * @param service The name of the service where the resources will be retrieved from.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to be retrieved
     * @param filter The filter to apply to the resources
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Retrieve(Persona persona, String service, String resource, String filter) {
        URL url = GetResourceURL(service, resource);

        RequestSpecification spec = given()
        .baseUri(url.toString())
        .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

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
     * @param persona The persona making the request.
     * @param service The name of the service where the resource will be retrieved from.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to be retrieved
     * @param reference The identifier of the resource to be retrieved
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Retrieve(Persona persona, String service, String resource, int reference) {
        return null;
    }

    
    /** 
     * 
     * Updates resources in the specified service using the 
     * provided payload.
     * 
     * @param persona The persona making the request.
     * @param service The name of the service where the resources will be updated.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to update
     * @param reference The identifier of the resource to be updated
     * @param payload The updated resource
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Update(Persona persona, String service, String resource, int reference, String payload) {
        return null;
    }

    
    /** 
     * 
     * Deletes resources in the specified service with the 
     * specified reference.
     * 
     * @param persona The persona making the request.
     * @param service The name of the service where the resources will be deleted.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to deleted
     * @param reference The identifier of the resource to be deleted
     * @return JpaRestApiResourceResponse
     */
    public JpaRestApiResourceResponse Delete(Persona persona, String service, String resource, int reference) {
        return null;
    }

    /** 
     * 
     * Retrieves resources in the specified service using the 
     * provided filter.
     * 
     * @param persona The persona making the request.
     * @param service The name of the service where the resources will be retrieved from.
     *  The service name must exist in the {@Link ServiceRegistry}
     * @param resource The name of the type of resource to be retrieved
     * @param filter The filter to apply to the resources
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

    private URL GetResourceURL(String service, String resource) {

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

    private URL GetServiceURL(String service, String path) {

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