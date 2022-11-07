package uk.gov.homeoffice.digital.sas.cucumberjparest;

import io.restassured.response.Response;
import java.net.URI;
import lombok.Getter;

/**
 * Returns the response associated with the provided url.
 */
public class JpaRestApiResponse {

  @Getter
  private final Response response;

  @Getter
  private final URI uri;

  public JpaRestApiResponse(URI uri, Response response) {
    this.uri = uri;
    this.response = response;
  }
}
