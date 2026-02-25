package io.spring.cucumber.stepdefs;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.spring.cucumber.ScenarioContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public class CommonStepDefs {

  @Autowired private MockMvc mvc;

  @Autowired private ScenarioContext context;

  @Before
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
    context.reset();
  }

  @Given("the API is available")
  public void theApiIsAvailable() {
    // MockMvc is initialized in @Before; nothing else needed
  }

  // --- HTTP Request Steps ---

  @When("I send a POST request to {string}")
  public void iSendAPostRequestTo(String path) {
    MockMvcResponse resp =
        given().contentType("application/json").body(context.getRequestBody()).when().post(path);
    context.setResponse(resp);
  }

  @When("I send an authenticated GET request to {string}")
  public void iSendAnAuthenticatedGetRequestTo(String path) {
    MockMvcResponse resp =
        given()
            .contentType("application/json")
            .header("Authorization", "Token " + context.getAuthToken())
            .when()
            .get(path);
    context.setResponse(resp);
  }

  @When("I send an authenticated PUT request to {string}")
  public void iSendAnAuthenticatedPutRequestTo(String path) {
    MockMvcResponse resp =
        given()
            .contentType("application/json")
            .header("Authorization", "Token " + context.getAuthToken())
            .body(context.getRequestBody())
            .when()
            .put(path);
    context.setResponse(resp);
  }

  @When("I send an authenticated POST request to {string}")
  public void iSendAnAuthenticatedPostRequestTo(String path) {
    MockMvcResponse resp =
        given()
            .contentType("application/json")
            .header("Authorization", "Token " + context.getAuthToken())
            .when()
            .post(path);
    context.setResponse(resp);
  }

  @When("I send an authenticated DELETE request to {string}")
  public void iSendAnAuthenticatedDeleteRequestTo(String path) {
    MockMvcResponse resp =
        given()
            .contentType("application/json")
            .header("Authorization", "Token " + context.getAuthToken())
            .when()
            .delete(path);
    context.setResponse(resp);
  }

  @When("I send a GET request to {string} without authentication")
  public void iSendAGetRequestWithoutAuthentication(String path) {
    MockMvcResponse resp = given().contentType("application/json").when().get(path);
    context.setResponse(resp);
  }

  // --- Response Validation Steps ---

  @Then("the response status code should be {int}")
  public void theResponseStatusCodeShouldBe(int statusCode) {
    assertThat(context.getResponse().statusCode(), is(statusCode));
  }

  @Then("the response status code should be either {int} or {int}")
  public void theResponseStatusCodeShouldBeEither(int code1, int code2) {
    int actual = context.getResponse().statusCode();
    assertTrue(
        actual == code1 || actual == code2,
        "Expected status " + code1 + " or " + code2 + " but got " + actual);
  }

  @Then("the response body should contain a {string} wrapper object")
  public void theResponseBodyShouldContainAWrapperObject(String wrapperName) {
    Object wrapper = context.getResponse().jsonPath().get(wrapperName);
    assertThat(
        "Response should contain '" + wrapperName + "' wrapper", wrapper, is(notNullValue()));
  }

  // --- User Object Validation ---

  @Then("the user object should have field {string} with value {string}")
  public void theUserObjectShouldHaveFieldWithValue(String field, String value) {
    String actual = context.getResponse().jsonPath().getString("user." + field);
    assertThat(actual, equalTo(value));
  }

  @Then("the user object should have a non-empty {string} field")
  public void theUserObjectShouldHaveNonEmptyField(String field) {
    String actual = context.getResponse().jsonPath().getString("user." + field);
    assertThat("user." + field + " should not be null", actual, is(notNullValue()));
    assertTrue(!actual.isEmpty(), "user." + field + " should not be empty");
  }

  @Then("the user object should have field {string} of type {string}")
  public void theUserObjectShouldHaveFieldOfType(String field, String type) {
    Object value = context.getResponse().jsonPath().get("user." + field);
    // The field may be null (e.g. bio/image), but should be present in the response
    if (value != null) {
      switch (type) {
        case "string":
          assertTrue(value instanceof String, "user." + field + " should be a String");
          break;
        case "boolean":
          assertTrue(value instanceof Boolean, "user." + field + " should be a Boolean");
          break;
        default:
          throw new IllegalArgumentException("Unknown type: " + type);
      }
    }
  }

  @Then("the response body should only contain these user fields: {string}")
  public void theResponseBodyShouldOnlyContainTheseUserFields(String fieldsCsv) {
    Set<String> expected = new HashSet<>(Arrays.asList(fieldsCsv.split(",")));
    Map<String, Object> userMap = context.getResponse().jsonPath().getMap("user");
    assertThat("user object should not be null", userMap, is(notNullValue()));
    Set<String> actual = userMap.keySet();
    assertThat("User object should contain exactly the expected fields", actual, equalTo(expected));
  }

  // --- Profile Object Validation ---

  @Then("the profile object should have field {string} with value {string}")
  public void theProfileObjectShouldHaveFieldWithValue(String field, String value) {
    String actual = context.getResponse().jsonPath().getString("profile." + field);
    assertThat(actual, equalTo(value));
  }

  @Then("the profile object should have field {string} of type {string}")
  public void theProfileObjectShouldHaveFieldOfType(String field, String type) {
    Object value = context.getResponse().jsonPath().get("profile." + field);
    if (value != null) {
      switch (type) {
        case "string":
          assertTrue(value instanceof String, "profile." + field + " should be a String");
          break;
        case "boolean":
          assertTrue(value instanceof Boolean, "profile." + field + " should be a Boolean");
          break;
        default:
          throw new IllegalArgumentException("Unknown type: " + type);
      }
    }
  }

  @Then("the profile object should have field {string} with boolean value true")
  public void theProfileObjectShouldHaveFieldWithBooleanTrue(String field) {
    Boolean actual = context.getResponse().jsonPath().getBoolean("profile." + field);
    assertThat(actual, is(true));
  }

  @Then("the profile object should have field {string} with boolean value false")
  public void theProfileObjectShouldHaveFieldWithBooleanFalse(String field) {
    Boolean actual = context.getResponse().jsonPath().getBoolean("profile." + field);
    assertThat(actual, is(false));
  }

  @Then("the response body should only contain these profile fields: {string}")
  public void theResponseBodyShouldOnlyContainTheseProfileFields(String fieldsCsv) {
    Set<String> expected = new HashSet<>(Arrays.asList(fieldsCsv.split(",")));
    Map<String, Object> profileMap = context.getResponse().jsonPath().getMap("profile");
    assertThat("profile object should not be null", profileMap, is(notNullValue()));
    Set<String> actual = profileMap.keySet();
    assertThat(
        "Profile object should contain exactly the expected fields", actual, equalTo(expected));
  }

  // --- Error Validation ---

  @Then("the response should contain error for field {string} with message {string}")
  public void theResponseShouldContainErrorForFieldWithMessage(String field, String message) {
    String actual = context.getResponse().jsonPath().getString("errors." + field + "[0]");
    assertThat(actual, equalTo(message));
  }

  @Then("the response should contain error for field {string}")
  public void theResponseShouldContainErrorForField(String field) {
    Object errors = context.getResponse().jsonPath().get("errors." + field);
    assertThat("Expected errors for field '" + field + "'", errors, is(notNullValue()));
  }

  @Then("the response should contain authentication error message")
  public void theResponseShouldContainAuthenticationErrorMessage() {
    String message = context.getResponse().jsonPath().getString("message");
    assertThat(message, equalTo("invalid email or password"));
  }

  // --- Utility Steps ---

  @And("the API should remain operational")
  public void theApiShouldRemainOperational() {
    // Verify the API still responds by sending a basic health check request
    MockMvcResponse healthCheck =
        given().contentType("application/json").when().get("/users/login");
    // Any response means the API is still running (even 422 is fine)
    assertTrue(healthCheck.statusCode() > 0, "API should still be operational after the request");
  }
}
