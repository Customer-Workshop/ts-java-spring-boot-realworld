package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.UserWithToken;
import io.spring.application.facade.UserApiFacade;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserApi.class)
@Import({
  WebSecurityConfig.class,
  JacksonCustomizations.class,
  ValidationAutoConfiguration.class,
  BCryptPasswordEncoder.class
})
public class CurrentUserApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private UserApiFacade userApiFacade;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_current_user_with_token() throws Exception {
    UserWithToken userWithToken = new UserWithToken(userData, token);
    when(userApiFacade.getCurrentUser(any(User.class), eq(token))).thenReturn(userWithToken);

    given()
        .header("Authorization", "Token " + token)
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username))
        .body("user.bio", equalTo(""))
        .body("user.image", equalTo(defaultAvatar))
        .body("user.token", equalTo(token));
  }

  @Test
  public void should_get_401_without_token() throws Exception {
    given().contentType("application/json").when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_get_401_with_invalid_token() throws Exception {
    String invalidToken = "asdfasd";
    when(jwtService.getSubFromToken(eq(invalidToken))).thenReturn(Optional.empty());
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + invalidToken)
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_update_current_user_profile() throws Exception {
    String newEmail = "newemail@example.com";
    String newBio = "updated";
    String newUsername = "newusernamee";

    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "user",
                new HashMap<String, Object>() {
                  {
                    put("email", newEmail);
                    put("bio", newBio);
                    put("username", newUsername);
                  }
                });
          }
        };

    when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());

    UserWithToken userWithToken = new UserWithToken(userData, token);
    when(userApiFacade.updateUser(any(User.class), any(), eq(token))).thenReturn(userWithToken);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/user")
        .then()
        .statusCode(200);
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String newEmail, final String newBio, final String newUsername) {
    return new HashMap<String, Object>() {
      {
        put(
            "user",
            new HashMap<String, Object>() {
              {
                put("email", newEmail);
                put("bio", newBio);
                put("username", newUsername);
              }
            });
      }
    };
  }

  @Test
  public void should_get_401_if_not_login() throws Exception {
    given()
        .contentType("application/json")
        .body(
            new HashMap<String, Object>() {
              {
                put("user", new HashMap<String, Object>());
              }
            })
        .when()
        .put("/user")
        .then()
        .statusCode(401);
  }
}
