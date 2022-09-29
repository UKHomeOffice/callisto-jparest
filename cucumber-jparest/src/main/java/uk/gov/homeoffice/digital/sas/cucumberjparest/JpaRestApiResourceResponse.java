package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.net.URL;

import io.restassured.response.Response;
import lombok.Getter;

/**
 * 
 * An extension of {@see JpaRestApiResponse} that enables
 * a reponse to be associated to both its request URL
 * and an additional base URL
 * 
 * This allows base urls to be used as a key
 * so that the {@see HttpResponseManager} can store
 * related responses.
 * 
 * E.g. consider paged resources
 * /resource/entity?page=1&size=10
 * /resource/entity?page=2&size=10
 * 
 * would both have their responses associated with
 * a base url of /resource/entity so that theu can later
 * be referenced as the first entity response and the
 * second entity response
 * 
 */
public class JpaRestApiResourceResponse extends JpaRestApiResponse {

    @Getter
    private final URL baseResourceURL;

    public JpaRestApiResourceResponse(URL baseResourceURL, URL url, Response response) {
        super(url, response);
        this.baseResourceURL = baseResourceURL;
    }
}
