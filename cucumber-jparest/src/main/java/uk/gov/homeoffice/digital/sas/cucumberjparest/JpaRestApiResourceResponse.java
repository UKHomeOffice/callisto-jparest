package uk.gov.homeoffice.digital.sas.cucumberjparest;

import io.restassured.response.Response;
import java.net.URL;
import lombok.Getter;

/**
 * An extension of {@link JpaRestApiResponse} that enables a reponse to be associated to both its
 * request URL and an additional base URL.
 *
 * <p>This allows base urls to be used as a key so that the {@link HttpResponseManager} can store
 * related responses.
 *
 * <p>E.g. consider paged resources /resource/entity?page=1&size=10 /resource/entity?page=2&size=10
 * would both have their responses associated with a base url of /resource/entity so that they can
 * later be referenced as the first entity response and the second entity response
 */
public class JpaRestApiResourceResponse extends JpaRestApiResponse {

  @Getter
  private final URL baseResourceUrl;

  public JpaRestApiResourceResponse(URL baseResourceUrl, URL url, Response response) {
    super(url, response);
    this.baseResourceUrl = baseResourceUrl;
  }
}
