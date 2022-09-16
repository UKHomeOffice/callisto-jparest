package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.util.Objects;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.NonNull;

import static io.restassured.RestAssured.given;

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
     * @return Response
     */
    public Response Create(Persona persona, String service, String resource, String payload) {
        String baseUrl = this.serviceRegistry.getService(service);
        String url = baseUrl + API_ROOT_PATH + resource;

        RequestSpecification spec = given()
            .body(payload)
            .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        return spec.post(url);
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
     * @return Response
     */
    public Response Retrieve(Persona persona, String service, String resource, String filter) {
        String baseUrl = this.serviceRegistry.getService(service);
        String url = baseUrl + API_ROOT_PATH + resource;

        RequestSpecification spec = given()
            .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        return spec.get(url);
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
     * @return Response
     */
    public Response Retrieve(Persona persona, String service, String resource, int reference) {
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
     * @return Response
     */
    public Response Update(Persona persona, String service, String resource, int reference, String payload) {
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
     * @return Response
     */
    public Response Delete(Persona persona, String service, String resource, int reference) {
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
     * @return Response
     */
    public Response Get(Persona persona, String service, String path) {
        String baseUrl = this.serviceRegistry.getService(service);
        String url = baseUrl + path;

        RequestSpecification spec = given()
            .queryParam("tenantId", "b7e813a2-bb28-11ec-8422-0242ac120002");

        addPersonaAuthToRequestSpecification(spec, persona);

        return spec.get(url);
    }
}