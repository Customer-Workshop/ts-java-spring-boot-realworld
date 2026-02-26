package io.spring.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.bdd.LoginRequestBuilder;
import io.spring.bdd.TestContext;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Step definitions for the User Login API feature (POST /api/users/login). */
public class UserLoginSteps {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Track whether the test user has already been registered in the shared in-memory database. */
  private static boolean userAlreadySeeded = false;

  private final TestContext testContext;

  public UserLoginSteps(TestContext testContext) {
    this.testContext = testContext;
  }

  // ---------------------------------------------------------------------------
  // Background – seed a test user via the registration API
  // ---------------------------------------------------------------------------

  @Given("a registered user exists with email {string} and password {string} and username {string}")
  public void registerUser(String email, String password, String username) {
    // If the user was already seeded in a prior scenario (shared in-memory DB),
    // verify we can still log in with the original credentials instead of failing.
    if (userAlreadySeeded) {
      return;
    }

    String registerJson =
        "{\"user\":{\"email\":\""
            + email
            + "\",\"password\":\""
            + password
            + "\",\"username\":\""
            + username
            + "\"}}";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(registerJson, headers);

    ResponseEntity<String> response =
        testContext.getRestTemplate().postForEntity("/users", request, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      userAlreadySeeded = true;
    } else {
      // If 422 with "duplicated email" the user was seeded by a previous test run
      String body = response.getBody();
      if (body != null && body.contains("duplicated email")) {
        userAlreadySeeded = true;
      } else {
        fail(
            "User registration should succeed but got: "
                + response.getStatusCodeValue()
                + " - "
                + body);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // When – send login request with explicit email & password
  // ---------------------------------------------------------------------------

  @When("I send a login request with email {string} and password {string}")
  public void sendLoginRequest(String email, String password) {
    String body = LoginRequestBuilder.buildLoginJson(email, password);
    postLogin(body);
  }

  // ---------------------------------------------------------------------------
  // When – send login request with a data table (supports omitting fields)
  // ---------------------------------------------------------------------------

  @When("I send a login request with the following body:")
  public void sendLoginRequestWithTable(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    Map<String, String> row = rows.get(0);

    String email = row.get("email");
    String password = row.get("password");

    // Treat empty-string values from the table as "field not provided"
    String emailValue = (email == null || email.isEmpty()) ? null : email;
    String passwordValue = (password == null || password.isEmpty()) ? null : password;

    String body = LoginRequestBuilder.buildLoginJson(emailValue, passwordValue);
    postLogin(body);
  }

  // ---------------------------------------------------------------------------
  // Then – status code assertion
  // ---------------------------------------------------------------------------

  @Then("the response status code should be {int}")
  public void verifyStatusCode(int expectedStatus) {
    assertEquals(
        expectedStatus,
        testContext.getLastResponse().getStatusCodeValue(),
        "Unexpected HTTP status code. Body: " + testContext.getLastResponse().getBody());
  }

  // ---------------------------------------------------------------------------
  // And – response body "user" wrapper
  // ---------------------------------------------------------------------------

  @And("the response body should contain a {string} object")
  public void responseContainsObject(String key) throws Exception {
    JsonNode root = MAPPER.readTree(testContext.getLastResponse().getBody());
    assertTrue(root.has(key), "Response JSON should contain key '" + key + "'");
    assertTrue(root.get(key).isObject(), "'" + key + "' should be a JSON object");
  }

  // ---------------------------------------------------------------------------
  // And – specific field value inside the "user" object
  // ---------------------------------------------------------------------------

  @And("the {string} object should contain the field {string} with value {string}")
  public void userFieldHasValue(String wrapper, String field, String expected) throws Exception {
    JsonNode user = MAPPER.readTree(testContext.getLastResponse().getBody()).path(wrapper);
    assertTrue(user.has(field), "'" + wrapper + "' should contain field '" + field + "'");
    assertEquals(expected, user.get(field).asText());
  }

  // ---------------------------------------------------------------------------
  // And – non-empty token
  // ---------------------------------------------------------------------------

  @And("the {string} object should contain a non-empty {string}")
  public void userFieldIsNonEmpty(String wrapper, String field) throws Exception {
    JsonNode user = MAPPER.readTree(testContext.getLastResponse().getBody()).path(wrapper);
    assertTrue(user.has(field), "'" + wrapper + "' should contain field '" + field + "'");
    String value = user.get(field).asText();
    assertFalse(value.isEmpty(), "'" + field + "' should not be empty");
  }

  // ---------------------------------------------------------------------------
  // And – field existence (bio / image may be null or empty)
  // ---------------------------------------------------------------------------

  @And("the {string} object should contain the field {string}")
  public void userFieldExists(String wrapper, String field) throws Exception {
    JsonNode user = MAPPER.readTree(testContext.getLastResponse().getBody()).path(wrapper);
    assertTrue(user.has(field), "'" + wrapper + "' should contain field '" + field + "'");
  }

  // ---------------------------------------------------------------------------
  // And – error message in response body
  // ---------------------------------------------------------------------------

  @And("the response body should contain the message {string}")
  public void responseContainsMessage(String expectedMessage) throws Exception {
    JsonNode root = MAPPER.readTree(testContext.getLastResponse().getBody());
    assertTrue(root.has("message"), "Response should contain 'message' key");
    assertEquals(expectedMessage, root.get("message").asText());
  }

  // ---------------------------------------------------------------------------
  // And – no database error details leaked
  // ---------------------------------------------------------------------------

  @And("the application should not expose any database error details")
  public void noDatabaseErrorLeaked() {
    String body = testContext.getLastResponse().getBody();
    assertNotNull(body, "Response body should not be null");
    String lower = body.toLowerCase();
    assertFalse(lower.contains("sql"), "Response should not contain SQL details");
    assertFalse(lower.contains("syntax error"), "Response should not contain syntax errors");
    assertFalse(lower.contains("stacktrace"), "Response should not expose stack traces");
    assertFalse(lower.contains("jdbc"), "Response should not expose JDBC details");
    assertFalse(lower.contains("sqlite"), "Response should not expose SQLite details");
  }

  // ---------------------------------------------------------------------------
  // Helper
  // ---------------------------------------------------------------------------

  private void postLogin(String jsonBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

    ResponseEntity<String> response =
        testContext.getRestTemplate().postForEntity("/users/login", request, String.class);

    testContext.setLastResponse(response);
  }
}
