package io.spring.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.bdd.HttpClientHelper;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

/**
 * Step definitions for the Current User API feature (GET /user, PUT /user).
 *
 * <p>These steps exercise the real Spring Boot application context with an in-memory SQLite
 * database so that JWT authentication, validation, and persistence are fully tested end-to-end.
 */
public class CurrentUserSteps {

  private static final AtomicInteger SCENARIO_COUNTER = new AtomicInteger(0);

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private HttpClientHelper httpClient;

  private HttpClientHelper getHttpClient() {
    if (httpClient == null) {
      httpClient = new HttpClientHelper(port, restTemplate);
    }
    return httpClient;
  }

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String registeredEmail;
  private String registeredPassword;
  private String registeredUsername;
  private String authToken;
  private ResponseEntity<String> response;

  // ──────────────────────────────────────────────
  // Background steps
  // ──────────────────────────────────────────────

  @Given("a registered user exists with username {string} and email {string} and password {string}")
  public void registerUser(String usernameBase, String emailBase, String password) {
    int id = SCENARIO_COUNTER.incrementAndGet();
    registeredUsername = usernameBase + id;
    registeredEmail = emailBase.replace("@", id + "@");
    registeredPassword = password;

    ResponseEntity<String> registerResponse =
        getHttpClient().registerUser(registeredUsername, registeredEmail, registeredPassword);
    assertTrue(
        registerResponse.getStatusCodeValue() == 201
            || registerResponse.getStatusCodeValue() == 200,
        "User registration should succeed but got status "
            + registerResponse.getStatusCodeValue()
            + ": "
            + registerResponse.getBody());
  }

  @Given("the user is logged in and has a valid auth token")
  public void loginAndObtainToken() throws Exception {
    ResponseEntity<String> loginResponse =
        getHttpClient().loginUser(registeredEmail, registeredPassword);
    assertEquals(
        200,
        loginResponse.getStatusCodeValue(),
        "Login should succeed but got: " + loginResponse.getBody());

    JsonNode root = objectMapper.readTree(loginResponse.getBody());
    authToken = root.path("user").path("token").asText();
    assertNotNull(authToken, "Auth token should not be null after login");
    assertTrue(!authToken.isEmpty(), "Auth token should not be empty after login");
  }

  // ──────────────────────────────────────────────
  // GET /user steps
  // ──────────────────────────────────────────────

  @When("the client sends a GET request to {string} with the auth token")
  public void getWithAuth(String path) {
    response = getHttpClient().getWithAuth(path, authToken);
  }

  @When("the client sends a GET request to {string} without an auth token")
  public void getWithoutAuth(String path) {
    response = getHttpClient().getWithoutAuth(path);
  }

  // ──────────────────────────────────────────────
  // PUT /user steps
  // ──────────────────────────────────────────────

  @When("the client sends a PUT request to {string} with the auth token and body:")
  public void putWithAuthAndBody(String path, String body) {
    response = getHttpClient().putWithAuth(path, authToken, body);
  }

  @When("the client sends a PUT request to {string} with a bio of {int} characters")
  public void putWithLongBio(String path, int length) {
    StringBuilder bio = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      bio.append('a');
    }
    String body = "{\"user\":{\"bio\":\"" + bio.toString() + "\"}}";
    response = getHttpClient().putWithAuth(path, authToken, body);
  }

  @When("the client sends a PUT request to {string} with an invalid auth token and body:")
  public void putWithInvalidAuthAndBody(String path, String body) {
    response = getHttpClient().putWithInvalidAuth(path, "invalid-expired-token-xyz", body);
  }

  // ──────────────────────────────────────────────
  // Response validation steps
  // ──────────────────────────────────────────────

  @Then("the response status code should be {int}")
  public void verifyStatusCode(int expectedStatus) {
    assertEquals(
        expectedStatus,
        response.getStatusCodeValue(),
        "Expected status "
            + expectedStatus
            + " but got "
            + response.getStatusCodeValue()
            + ". Body: "
            + response.getBody());
  }

  @And("the response should contain a {string} object")
  public void verifyResponseContainsObject(String objectName) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    assertTrue(
        root.has(objectName),
        "Response should contain a '" + objectName + "' object. Body: " + response.getBody());
    assertTrue(
        root.get(objectName).isObject(),
        "'" + objectName + "' should be a JSON object. Body: " + response.getBody());
  }

  @And("the user object {string} should match the registered email")
  public void verifyUserEmailMatchesRegistered(String field) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    JsonNode userNode = root.path("user");
    assertTrue(userNode.has(field), "User object should contain field '" + field + "'");
    assertEquals(
        registeredEmail, userNode.get(field).asText(), "Email should match the registered email");
  }

  @And("the user object {string} should match the registered username")
  public void verifyUserUsernameMatchesRegistered(String field) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    JsonNode userNode = root.path("user");
    assertTrue(userNode.has(field), "User object should contain field '" + field + "'");
    assertEquals(
        registeredUsername,
        userNode.get(field).asText(),
        "Username should match the registered username");
  }

  @And("the user object should contain the field {string} with value {string}")
  public void verifyUserFieldValue(String field, String expectedValue) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    JsonNode userNode = root.path("user");
    assertTrue(userNode.has(field), "User object should contain field '" + field + "'");
    assertEquals(
        expectedValue,
        userNode.get(field).asText(),
        "Field '" + field + "' should equal '" + expectedValue + "'");
  }

  @And("the user object should contain the field {string}")
  public void verifyUserFieldExists(String field) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    JsonNode userNode = root.path("user");
    assertTrue(
        userNode.has(field),
        "User object should contain field '" + field + "'. Body: " + response.getBody());
  }

  @And("the user object {string} field should have length {int}")
  public void verifyUserFieldLength(String field, int expectedLength) throws Exception {
    JsonNode root = objectMapper.readTree(response.getBody());
    JsonNode userNode = root.path("user");
    assertTrue(userNode.has(field), "User object should contain field '" + field + "'");
    String value = userNode.get(field).asText();
    assertEquals(
        expectedLength,
        value.length(),
        "Field '"
            + field
            + "' should have length "
            + expectedLength
            + " but was "
            + value.length());
  }
}
