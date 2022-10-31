package uk.gov.homeoffice.digital.sas.cucumberjparest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Class used for tracking Responses to requests made to a specific path
 */
@Component
public class HttpResponseManager {

  private final Map<URL, ArrayList<Response>> responses = new HashMap<>();

  // Provides quick access to the last response added
  @Getter
  private Response lastResponse;

  /**
   * Tracks the response for the given path. The path is used as a key for an array of responses for
   * the given path. This means that responses to GET, PUT, POST and delete methods will not be
   * distinguishable but this is intentional.
   * <p>
   * The response for a specific path, regadless of verb is retrieved by it's ordinal.
   *
   * @param url      The path the response was received from
   * @param response The response received
   * @return Response
   */
  public Response addResponse(URL url, Response response) {

    // If this is the first time the path has been used
    // initialise an ArrayList for storing the responses
    if (!responses.containsKey(url)) {
      responses.put(url, new ArrayList<>());
    }

    ArrayList<Response> pathResponses = responses.get(url);
    assertThat(pathResponses.add(response)).isTrue();
    this.lastResponse = response;
    return response;
  }

  public Response getResponse(URL url, int position) {
    if (!responses.containsKey(url)) {
      fail("No responses where logged for %s", url);
    }

    ArrayList<Response> pathResponses = responses.get(url);

    // Treat -1 as last index. As keys are only added when
    // responses are added. The array list will at least have a size
    // of 1.
    if (position == -1) {
      position = pathResponses.size() - 1;
    }

    Response response = null;
    try {
      response = pathResponses.get(position);
    } catch (IndexOutOfBoundsException ex) {
      // In the messaging bare in mind that indexes are zero based so
      // the position needs adjusting to match the order the uer will have
      // asked for.
      fail("Only %i responses were logged for %s but you asked for %i", pathResponses.size(), url,
          position + 1);
    }
    return response;
  }
}