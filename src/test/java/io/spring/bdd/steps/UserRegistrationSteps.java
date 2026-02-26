package io.spring.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.bdd.HttpClientHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/** Step definitions for the User Registration API (POST /api/users) feature. */
public class UserRegistrationSteps {

  @Autowired private HttpClientHelper http;

  private Map<String, Object> requestPayload;

  // ------------------------------------------------------------------
  // Background
  // ------------------------------------------------------------------

  @Given("the application is running")
  public void theApplicationIsRunning() {
    // The Spring Boot test context is already started via CucumberSpringConfiguration.
    assertNotNull(http, "HttpClientHelper should be injected by Spring");
  }

  // ------------------------------------------------------------------
  // Preparing registration payloads
  // ------------------------------------------------------------------

  @Given(
      "I prepare a registration request with username {string}, email {string}, and password"
          + " {string}")
  public void iPrepareARegistrationRequest(String username, String email, String password) {
    requestPayload = buildUserPayload(email, username, password);
  }

  @Given("a user already exists with username {string}, email {string}, and password {string}")
  public void aUserAlreadyExists(String username, String email, String password)
      throws IOException, InterruptedException {
    Map<String, Object> payload = buildUserPayload(email, username, password);
    http.post("/users", payload);
    assertEquals(201, http.statusCode(), "Pre-existing user creation should succeed");
  }

  @Given("I prepare a registration request with {word} omitted")
  public void iPrepareARegistrationRequestWithFieldOmitted(String field) {
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", "test@example.com");
    userMap.put("username", "testuser");
    userMap.put("password", "password123");
    userMap.remove(field);

    requestPayload = new HashMap<>();
    requestPayload.put("user", userMap);
  }

  @Given("I prepare a registration request with {word} set to {string}")
  public void iPrepareARegistrationRequestWithFieldSetTo(String field, String value) {
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", "empty@example.com");
    userMap.put("username", "emptyuser");
    userMap.put("password", "password123");
    userMap.put(field, value);

    requestPayload = new HashMap<>();
    requestPayload.put("user", userMap);
  }

  @Given(
      "I prepare a registration request with a username of {int} characters, email {string},"
          + " and password {string}")
  public void iPrepareARegistrationRequestWithLongUsername(
      int length, String email, String password) {
    String longUsername = "a".repeat(length);
    requestPayload = buildUserPayload(email, longUsername, password);
  }

  // ------------------------------------------------------------------
  // Sending the request
  // ------------------------------------------------------------------

  @When("I send a POST request to {string}")
  public void iSendAPostRequest(String path) throws IOException, InterruptedException {
    http.post(path, requestPayload);
  }

  // ------------------------------------------------------------------
  // Response assertions
  // ------------------------------------------------------------------

  @Then("the response status code should be {int}")
  public void theResponseStatusCodeShouldBe(int expectedStatus) {
    assertEquals(
        expectedStatus,
        http.statusCode(),
        "Expected HTTP " + expectedStatus + " but got " + http.statusCode());
  }

  @Then("the response status code should be one of {int} or {int}")
  public void theResponseStatusCodeShouldBeOneOf(int status1, int status2) {
    int actual = http.statusCode();
    assertTrue(
        actual == status1 || actual == status2,
        "Expected HTTP " + status1 + " or " + status2 + " but got " + actual);
  }

  @And("the response body should contain a {string} object")
  public void theResponseBodyShouldContainObject(String key) throws IOException {
    JsonNode root = http.responseBodyAsJson();
    assertTrue(root.has(key), "Response should contain a '" + key + "' object");
    assertTrue(root.get(key).isObject(), "'" + key + "' should be a JSON object");
  }

  @And("the {string} object should have the following fields:")
  public void theObjectShouldHaveFields(String objectPath, DataTable dataTable) throws IOException {
    JsonNode root = http.responseBodyAsJson();
    JsonNode userNode = root.get(objectPath);
    assertNotNull(userNode, "Expected '" + objectPath + "' node in response");

    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> row : rows) {
      String field = row.get("field");
      String type = row.get("type");
      assertTrue(userNode.has(field), "'" + objectPath + "' should contain field '" + field + "'");
      JsonNode fieldNode = userNode.get(field);
      assertFieldType(objectPath + "." + field, fieldNode, type);
    }
  }

  @And("the {string} should be {string}")
  public void theFieldShouldBe(String fieldPath, String expectedValue) throws IOException {
    JsonNode root = http.responseBodyAsJson();
    JsonNode node = resolveJsonPath(root, fieldPath);
    assertNotNull(node, "Field '" + fieldPath + "' should exist in response");
    assertEquals(expectedValue, node.asText(), "Field '" + fieldPath + "' value mismatch");
  }

  @And("the {string} should not be empty")
  public void theFieldShouldNotBeEmpty(String fieldPath) throws IOException {
    JsonNode root = http.responseBodyAsJson();
    JsonNode node = resolveJsonPath(root, fieldPath);
    assertNotNull(node, "Field '" + fieldPath + "' should exist in response");
    assertFalse(node.asText().isEmpty(), "Field '" + fieldPath + "' should not be empty");
  }

  @And("the response body should contain error for field {string}")
  public void theResponseBodyShouldContainErrorForField(String field) throws IOException {
    JsonNode root = http.responseBodyAsJson();
    JsonNode errorsNode = root.get("errors");
    assertNotNull(errorsNode, "Response should contain an 'errors' object");
    assertTrue(
        errorsNode.has(field),
        "Errors should reference field '"
            + field
            + "'. Actual errors: "
            + errorsNode.toPrettyString());
  }

  @And("the application should not have a database error")
  public void theApplicationShouldNotHaveADatabaseError() {
    int status = http.statusCode();
    assertTrue(status < 500, "Expected no server error but got HTTP " + status);
  }

  @And("the response should be valid JSON")
  public void theResponseShouldBeValidJson() throws IOException {
    assertDoesNotThrow(() -> http.responseBodyAsJson(), "Response body should be valid JSON");
  }

  // ------------------------------------------------------------------
  // Private helpers
  // ------------------------------------------------------------------

  private Map<String, Object> buildUserPayload(String email, String username, String password) {
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", email);
    userMap.put("username", username);
    userMap.put("password", password);

    Map<String, Object> payload = new HashMap<>();
    payload.put("user", userMap);
    return payload;
  }

  /** Resolves a dot-separated path (e.g. "user.email") against a {@link JsonNode} tree. */
  private JsonNode resolveJsonPath(JsonNode root, String path) {
    String[] parts = path.split("\\.");
    JsonNode current = root;
    for (String part : parts) {
      if (current == null) {
        return null;
      }
      current = current.get(part);
    }
    return current;
  }

  private void assertFieldType(String fieldName, JsonNode node, String expectedType) {
    switch (expectedType.toLowerCase()) {
      case "string":
        assertTrue(
            node.isTextual() || node.isNull(),
            fieldName + " should be a string but was " + node.getNodeType());
        break;
      case "number":
        assertTrue(
            node.isNumber(), fieldName + " should be a number but was " + node.getNodeType());
        break;
      case "boolean":
        assertTrue(
            node.isBoolean(), fieldName + " should be a boolean but was " + node.getNodeType());
        break;
      default:
        fail("Unknown expected type: " + expectedType);
    }
  }
}
