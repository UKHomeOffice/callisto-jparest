package uk.gov.homeoffice.digital.sas.cucumberjparest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import io.restassured.response.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Class used for tracking Responses to requests made to a specific path.
 */
@Component
@SuppressWarnings("squid:S5960")// Assertions are needed in this test library
public class HttpResponseManager {

  private final Map<URI, ArrayList<Response>> responses = new HashMap<>();

  // Provides quick access to the last response added
  @Getter
  private Response lastResponse;

  /**
   * Tracks the response for the given path. The path is used as a key for an array of responses for
   * the given path. This means that responses to GET, PUT, POST and delete methods will not be
   * distinguishable but this is intentional.
   *
   * <p>The response for a specific path, regardless of verb is retrieved by its ordinal.
   *
   * @param uri      The path the response was received from
   * @param response The response received
   * @return Response
   */
  public Response addResponse(URI uri, Response response) {

    // If this is the first time the path has been used
    // initialise an ArrayList for storing the responses
    responses.computeIfAbsent(uri, k -> new ArrayList<>());

    ArrayList<Response> pathResponses = responses.get(uri);
    assertThat(pathResponses.add(response)).isTrue();
    this.lastResponse = response;
    return response;
  }

  /**
   * Returns the response for the specified {@code uri}, at the specified {@code position}
   *
   * @param uri The path the response to be retrieved from
   * @param position The 0-based ordinal of the response to retrieve the resource from. -1 should be
   *                 used to get the last element
   * @return Response
   */
  public Response getResponse(URI uri, int position) {
    if (!responses.containsKey(uri)) {
      fail("No responses were logged for %s", uri);
    }

    ArrayList<Response> pathResponses = responses.get(uri);

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
      // In the messaging bear in mind that indexes are zero based so
      // the position needs adjusting to match the order the user will have
      // asked for.
      fail("Only %i responses were logged for %s but you asked for %i", pathResponses.size(), uri,
          position + 1);
    }
    return response;
  }
}