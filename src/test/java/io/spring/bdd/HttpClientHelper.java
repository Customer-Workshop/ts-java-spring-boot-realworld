package io.spring.bdd;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Shared HTTP client helper for Cucumber BDD integration tests. */
public class HttpClientHelper {

  private final int port;
  private final TestRestTemplate restTemplate;

  public HttpClientHelper(int port, TestRestTemplate restTemplate) {
    this.port = port;
    this.restTemplate = restTemplate;
  }

  public String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Register a new user via POST /users.
   *
   * @return the raw response entity
   */
  public ResponseEntity<String> registerUser(String username, String email, String password) {
    String body =
        String.format(
            "{\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}}",
            username, email, password);
    HttpHeaders headers = jsonHeaders();
    HttpEntity<String> request = new HttpEntity<>(body, headers);
    return restTemplate.postForEntity(baseUrl() + "/users", request, String.class);
  }

  /**
   * Login an existing user via POST /users/login.
   *
   * @return the raw response entity
   */
  public ResponseEntity<String> loginUser(String email, String password) {
    String body =
        String.format("{\"user\":{\"email\":\"%s\",\"password\":\"%s\"}}", email, password);
    HttpHeaders headers = jsonHeaders();
    HttpEntity<String> request = new HttpEntity<>(body, headers);
    return restTemplate.postForEntity(baseUrl() + "/users/login", request, String.class);
  }

  /**
   * Send an authenticated GET request.
   *
   * @param path the API path (e.g. "/user")
   * @param token the JWT token
   * @return the raw response entity
   */
  public ResponseEntity<String> getWithAuth(String path, String token) {
    HttpHeaders headers = jsonHeaders();
    headers.set("Authorization", "Token " + token);
    HttpEntity<String> request = new HttpEntity<>(headers);
    return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, request, String.class);
  }

  /**
   * Send an unauthenticated GET request.
   *
   * @param path the API path
   * @return the raw response entity
   */
  public ResponseEntity<String> getWithoutAuth(String path) {
    HttpHeaders headers = jsonHeaders();
    HttpEntity<String> request = new HttpEntity<>(headers);
    return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, request, String.class);
  }

  /**
   * Send an authenticated PUT request with a JSON body.
   *
   * @param path the API path
   * @param token the JWT token
   * @param body the JSON body string
   * @return the raw response entity
   */
  public ResponseEntity<String> putWithAuth(String path, String token, String body) {
    HttpHeaders headers = jsonHeaders();
    headers.set("Authorization", "Token " + token);
    HttpEntity<String> request = new HttpEntity<>(body, headers);
    return restTemplate.exchange(baseUrl() + path, HttpMethod.PUT, request, String.class);
  }

  /**
   * Send an unauthenticated PUT request with a JSON body.
   *
   * @param path the API path
   * @param body the JSON body string
   * @return the raw response entity
   */
  public ResponseEntity<String> putWithInvalidAuth(String path, String invalidToken, String body) {
    HttpHeaders headers = jsonHeaders();
    headers.set("Authorization", "Token " + invalidToken);
    HttpEntity<String> request = new HttpEntity<>(body, headers);
    return restTemplate.exchange(baseUrl() + path, HttpMethod.PUT, request, String.class);
  }

  private HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
