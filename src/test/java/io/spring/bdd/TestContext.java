package io.spring.bdd;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Shared test context that holds state between Cucumber step definitions. Stores the most recent
 * HTTP response so that assertion steps can inspect it.
 */
@Component
public class TestContext {

  private ResponseEntity<String> lastResponse;
  private final TestRestTemplate restTemplate;

  public TestContext(TestRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public TestRestTemplate getRestTemplate() {
    return restTemplate;
  }

  public ResponseEntity<String> getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(ResponseEntity<String> response) {
    this.lastResponse = response;
  }
}
