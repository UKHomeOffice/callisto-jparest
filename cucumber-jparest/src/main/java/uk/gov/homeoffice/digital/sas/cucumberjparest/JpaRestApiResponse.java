package uk.gov.homeoffice.digital.sas.cucumberjparest;

import java.net.URL;

import io.restassured.response.Response;
import lombok.Getter;

public class JpaRestApiResponse {

    @Getter
    private final Response response;

    @Getter
    private final URL url;

    public JpaRestApiResponse(URL url, Response response) {
        this.url = url;
        this.response = response;
    }
}