package io.spring.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Shared HTTP client helper for Cucumber BDD integration tests.
 *
 * <p>Uses a plain {@link ObjectMapper} for reading responses so that the global {@code
 * UNWRAP_ROOT_VALUE} setting does not interfere with generic JSON parsing.
 */
public class HttpClientHelper {

  private final int port;
  private final ObjectMapper plainObjectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private HttpResponse<String> lastResponse;

  public HttpClientHelper(int port) {
    this.port = port;
  }

  /** Returns the base URL of the running application (e.g. {@code http://localhost:8080}). */
  public String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Sends an HTTP POST request with a JSON body to the given path and stores the response.
   *
   * @param path the request path (e.g. {@code /users})
   * @param body the request body as a Java object that will be serialised to JSON
   */
  public void post(String path, Object body) throws IOException, InterruptedException {
    String json = plainObjectMapper.writeValueAsString(body);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
    lastResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /** Returns the HTTP status code of the most recent response. */
  public int statusCode() {
    return lastResponse.statusCode();
  }

  /** Returns the response body as a raw string. */
  public String responseBody() {
    return lastResponse.body();
  }

  /** Parses the most recent response body as a Jackson {@link JsonNode}. */
  public JsonNode responseBodyAsJson() throws IOException {
    return plainObjectMapper.readTree(lastResponse.body());
  }
}
