package io.spring.bdd;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Holds shared mutable state across Cucumber step definitions within a single scenario. */
@Component
public class SharedTestState {

  private Response lastResponse;
  private String currentToken;
  private final Map<String, String> userTokens = new HashMap<>();

  public Response getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(Response lastResponse) {
    this.lastResponse = lastResponse;
  }

  public String getCurrentToken() {
    return currentToken;
  }

  public void setCurrentToken(String currentToken) {
    this.currentToken = currentToken;
  }

  public Map<String, String> getUserTokens() {
    return userTokens;
  }

  /** Reset all state between scenarios. */
  public void reset() {
    lastResponse = null;
    currentToken = null;
    userTokens.clear();
  }
}
