package io.spring.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.spring.bdd.SharedTestState;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

public class ProfileSteps {

  @LocalServerPort private int port;

  @Autowired private SharedTestState state;

  @Before
  public void setUp() {
    state.reset();
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  // ---------- Registration & Authentication ----------

  @Given("the following users are registered:")
  public void theFollowingUsersAreRegistered(DataTable dataTable) {
    List<Map<String, String>> users = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> userData : users) {
      String username = userData.get("username");
      String email = userData.get("email");
      String password = userData.get("password");

      String registerBody =
          String.format(
              "{\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}}",
              username, email, password);

      Response response =
          given().contentType(ContentType.JSON).body(registerBody).when().post("/users");

      if (response.getStatusCode() == 201) {
        state.getUserTokens().put(username, response.jsonPath().getString("user.token"));
      } else {
        // User may already exist from a previous scenario; log in instead
        String loginBody =
            String.format("{\"user\":{\"email\":\"%s\",\"password\":\"%s\"}}", email, password);
        Response loginResponse =
            given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
        if (loginResponse.getStatusCode() == 200) {
          state.getUserTokens().put(username, loginResponse.jsonPath().getString("user.token"));
        }
      }
    }
  }

  @Given("I am authenticated as {string}")
  public void iAmAuthenticatedAs(String username) {
    String token = state.getUserTokens().get(username);
    if (token == null) {
      throw new IllegalStateException(
          "No token found for user '" + username + "'. Was the user registered?");
    }
    state.setCurrentToken(token);
  }

  // ---------- GET /profiles/:username ----------

  @When("I request the profile of {string}")
  public void iRequestTheProfileOf(String username) {
    Response response =
        given()
            .header("Authorization", "Token " + state.getCurrentToken())
            .when()
            .get("/profiles/{username}", username);
    state.setLastResponse(response);
  }

  @When("I request the profile of {string} without authentication")
  public void iRequestTheProfileOfWithoutAuthentication(String username) {
    Response response = given().when().get("/profiles/{username}", username);
    state.setLastResponse(response);
  }

  // ---------- POST /profiles/:username/follow ----------

  @When("I follow the user {string}")
  public void iFollowTheUser(String username) {
    Response response =
        given()
            .header("Authorization", "Token " + state.getCurrentToken())
            .contentType(ContentType.JSON)
            .when()
            .post("/profiles/{username}/follow", username);
    state.setLastResponse(response);
  }

  @When("I follow the user {string} without authentication")
  public void iFollowTheUserWithoutAuthentication(String username) {
    Response response =
        given().contentType(ContentType.JSON).when().post("/profiles/{username}/follow", username);
    state.setLastResponse(response);
  }

  @Given("I have already followed the user {string}")
  public void iHaveAlreadyFollowedTheUser(String username) {
    given()
        .header("Authorization", "Token " + state.getCurrentToken())
        .contentType(ContentType.JSON)
        .when()
        .post("/profiles/{username}/follow", username);
  }

  // ---------- DELETE /profiles/:username/follow ----------

  @When("I unfollow the user {string}")
  public void iUnfollowTheUser(String username) {
    Response response =
        given()
            .header("Authorization", "Token " + state.getCurrentToken())
            .contentType(ContentType.JSON)
            .when()
            .delete("/profiles/{username}/follow", username);
    state.setLastResponse(response);
  }

  @When("I unfollow the user {string} without authentication")
  public void iUnfollowTheUserWithoutAuthentication(String username) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .when()
            .delete("/profiles/{username}/follow", username);
    state.setLastResponse(response);
  }

  // ---------- Profile Response Assertions ----------

  @And("the profile should have the following fields:")
  public void theProfileShouldHaveTheFollowingFields(DataTable dataTable) {
    List<Map<String, String>> fields = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> field : fields) {
      String fieldName = field.get("field");
      String fieldType = field.get("type");
      Object value = state.getLastResponse().jsonPath().get("profile." + fieldName);
      switch (fieldType) {
        case "string":
          // Value can be null for optional string fields (bio, image)
          if (value != null) {
            assertThat("Field '" + fieldName + "' should be a string", value, isA(String.class));
          }
          break;
        case "boolean":
          assertThat("Field '" + fieldName + "' should be a boolean", value, isA(Boolean.class));
          break;
        default:
          throw new IllegalArgumentException("Unsupported field type: " + fieldType);
      }
    }
  }

  @And("the profile username should be {string}")
  public void theProfileUsernameShouldBe(String expectedUsername) {
    String actualUsername = state.getLastResponse().jsonPath().getString("profile.username");
    assertThat(actualUsername, equalTo(expectedUsername));
  }

  @And("the profile {string} field should be true")
  public void theProfileFieldShouldBeTrue(String fieldName) {
    Boolean value = state.getLastResponse().jsonPath().getBoolean("profile." + fieldName);
    assertThat("profile." + fieldName + " should be true", value, equalTo(true));
  }

  @And("the profile {string} field should be false")
  public void theProfileFieldShouldBeFalse(String fieldName) {
    Boolean value = state.getLastResponse().jsonPath().getBoolean("profile." + fieldName);
    assertThat("profile." + fieldName + " should be false", value, equalTo(false));
  }
}
