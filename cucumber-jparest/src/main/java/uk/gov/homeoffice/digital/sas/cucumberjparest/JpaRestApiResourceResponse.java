package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.net.URL;

import io.restassured.response.Response;
import lombok.Getter;

public class JpaRestApiResourceResponse extends JpaRestApiResponse {

    @Getter
    private final URL baseResourceURL;

    public JpaRestApiResourceResponse(URL baseResourceURL, URL url, Response response) {
        super(url, response);
        this.baseResourceURL = baseResourceURL;
    }
}