package io.spring.cucumber;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Shared state between Cucumber step definitions within a single scenario. This class holds the
 * request/response context so that step definitions can communicate.
 */
@Component
public class ScenarioContext {

  private MockMvcResponse response;
  private Map<String, Object> requestBody;
  private String authToken;
  private String authenticatedUserId;
  private String authenticatedUsername;

  public void reset() {
    this.response = null;
    this.requestBody = null;
    this.authToken = null;
    this.authenticatedUserId = null;
    this.authenticatedUsername = null;
  }

  public MockMvcResponse getResponse() {
    return response;
  }

  public void setResponse(MockMvcResponse response) {
    this.response = response;
  }

  public Map<String, Object> getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(Map<String, Object> requestBody) {
    this.requestBody = requestBody;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getAuthenticatedUserId() {
    return authenticatedUserId;
  }

  public void setAuthenticatedUserId(String authenticatedUserId) {
    this.authenticatedUserId = authenticatedUserId;
  }

  public String getAuthenticatedUsername() {
    return authenticatedUsername;
  }

  public void setAuthenticatedUsername(String authenticatedUsername) {
    this.authenticatedUsername = authenticatedUsername;
  }
}
