package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;

/**
 * Class used for tracking Responses to requests made to
 * a specific path
*/
@Component
public class HttpResponseManager {

    private Map<String, ArrayList<Response>> responses = new HashMap<>();
    
    // Provides quick access to the last response added
    @Getter
    private Response lastResponse;

    
    /** 
     * Tracks the response for the given path. The path
     * is used as a key for an array of responses for
     * the given path. This means that responses to GET,
     * PUT, POST and delete methods will not be distinguishable
     * but this is intentional. 
     * 
     * The response for a specific path, regadless of verb
     * is retrieved by it's ordinal.
     * 
     * @param path The path the reponse was received from
     * @param response The response received
     * @return Response
     */
    public Response addResponse(String path, Response response) {

        // If this is the first time the path has been used
        // initialise an ArrayList for storing the responses
        if (!responses.containsKey(path)) {
            responses.put(path, new ArrayList<Response>());
        }
        
        ArrayList<Response> pathResponses = responses.get(path);
        assertThat(pathResponses.add(response)).isTrue();
        this.lastResponse = response;
        return response;
    }

    
    /** 
     *
     * Wrapper for the RestAssured given static. 
     * Start building the request part of the test io.restassured.specification. E.g.
     * 
     * RestAssured was chosen as it is familiar to our QA commmunity but will
     * sometimes be used within the project in a way that doesn't suit the 
     * Given-When-Then style. This static can be importanted as an alternative
     * 
     * @return RequestSpecification
     */
    public static RequestSpecification ConstructRequest() {
        return given();
    }
}