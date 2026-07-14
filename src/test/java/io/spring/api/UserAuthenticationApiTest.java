package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.DefaultJwtService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Spec-driven suite for GitHub issue #189 — "User Registration & Authentication".
 *
 * <p>Covers acceptance criteria AC1–AC13 end to end against the REST user endpoints ({@code POST
 * /users}, {@code POST /users/login}, {@code GET /user}, {@code PUT /user}). The real {@link
 * DefaultJwtService} is wired in (instead of a mock) so JWT issuance, the authentication filter,
 * and the round-trip in AC13 exercise production token behaviour. Per the spec, the JWT string
 * itself is never asserted verbatim — only that a token is present, non-empty, and usable on a
 * follow-up authenticated request.
 */
@WebMvcTest({UsersApi.class, CurrentUserApi.class})
@Import({
  WebSecurityConfig.class,
  JacksonCustomizations.class,
  UserService.class,
  UserQueryService.class,
  DefaultJwtService.class,
  ValidationAutoConfiguration.class
})
public class UserAuthenticationApiTest {

  @Autowired private MockMvc mvc;

  @Autowired private JwtService jwtService;

  @Autowired private PasswordEncoder passwordEncoder;

  @MockBean private UserRepository userRepository;

  @MockBean private UserReadService userReadService;

  private String email;
  private String username;
  private String password;
  private String bio;
  private String defaultAvatar;

  private User user;
  private UserData userData;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);

    email = "jane@example.com";
    username = "janedoe";
    password = "secretpass";
    bio = "";
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new User(email, username, passwordEncoder.encode(password), bio, defaultAvatar);
    userData = new UserData(user.getId(), email, username, bio, defaultAvatar);

    // The JWT filter resolves the authenticated principal from the persisted user.
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
  }

  // ---------------------------------------------------------------------------
  // Register — POST /users
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName(
      "AC1: register with unique username/email + valid password returns 200-family with a"
          + " non-empty token")
  public void ac1_register_success_returns_user_with_non_empty_token() {
    when(userReadService.findById(any())).thenReturn(userData);

    given()
        .contentType("application/json")
        .body(registerBody(email, username, password))
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("user.username", equalTo(username))
        .body("user.email", equalTo(email))
        .body("user.token", not(nullValue()))
        .body("user.token", not(equalTo("")));
  }

  @Test
  @DisplayName("AC2: register with a duplicate email returns 422")
  public void ac2_register_duplicate_email_returns_422() {
    when(userRepository.findByEmail(eq(email)))
        .thenReturn(Optional.of(new User(email, "someoneelse", "pw", "", "")));

    given()
        .contentType("application/json")
        .body(registerBody(email, username, password))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("duplicated email"));
  }

  @Test
  @DisplayName("AC3: register with a duplicate username returns 422")
  public void ac3_register_duplicate_username_returns_422() {
    when(userRepository.findByUsername(eq(username)))
        .thenReturn(Optional.of(new User("someoneelse@example.com", username, "pw", "", "")));

    given()
        .contentType("application/json")
        .body(registerBody(email, username, password))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.username[0]", equalTo("duplicated username"));
  }

  @Test
  @DisplayName("AC4: register with a blank username returns 422")
  public void ac4_register_blank_username_returns_422() {
    given()
        .contentType("application/json")
        .body(registerBody(email, "", password))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.username[0]", equalTo("can't be empty"));
  }

  @Test
  @DisplayName("AC4: register with a blank email returns 422")
  public void ac4_register_blank_email_returns_422() {
    given()
        .contentType("application/json")
        .body(registerBody("", username, password))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("can't be empty"));
  }

  @Test
  @DisplayName("AC4: register with a blank password returns 422")
  public void ac4_register_blank_password_returns_422() {
    given()
        .contentType("application/json")
        .body(registerBody(email, username, ""))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.password[0]", equalTo("can't be empty"));
  }

  @Test
  @DisplayName("AC5: register with a malformed email returns 422")
  public void ac5_register_malformed_email_returns_422() {
    given()
        .contentType("application/json")
        .body(registerBody("not-an-email", username, password))
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"));
  }

  // ---------------------------------------------------------------------------
  // Login — POST /users/login
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("AC6: login with correct email + password returns 200 with a non-empty token")
  public void ac6_login_success_returns_non_empty_token() {
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    given()
        .contentType("application/json")
        .body(loginBody(email, password))
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username))
        .body("user.token", not(nullValue()))
        .body("user.token", not(equalTo("")));
  }

  @Test
  @DisplayName("AC7: login with a wrong password returns the auth-failure status and no token")
  public void ac7_login_wrong_password_returns_422_and_no_token() {
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    given()
        .contentType("application/json")
        .body(loginBody(email, "wrong-password"))
        .when()
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("message", equalTo("invalid email or password"))
        .body("user", nullValue());
  }

  @Test
  @DisplayName("AC8: login with an unknown email returns the auth-failure status and no token")
  public void ac8_login_unknown_email_returns_422_and_no_token() {
    when(userRepository.findByEmail(eq("nobody@example.com"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .body(loginBody("nobody@example.com", password))
        .when()
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("message", equalTo("invalid email or password"))
        .body("user", nullValue());
  }

  // ---------------------------------------------------------------------------
  // Current user — GET /user
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("AC9: GET /user with a valid token returns 200 with the user's details and a token")
  public void ac9_current_user_with_valid_token_returns_200() {
    String token = jwtService.toToken(user);

    given()
        .header("Authorization", "Token " + token)
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username))
        .body("user.token", equalTo(token));
  }

  @Test
  @DisplayName("AC10: GET /user with no token returns 401")
  public void ac10_current_user_without_token_returns_401() {
    given().contentType("application/json").when().get("/user").then().statusCode(401);
  }

  @Test
  @DisplayName("AC10: GET /user with an invalid token returns 401")
  public void ac10_current_user_with_invalid_token_returns_401() {
    given()
        .header("Authorization", "Token not-a-valid-jwt")
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(401);
  }

  // ---------------------------------------------------------------------------
  // Update current user — PUT /user
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName(
      "AC11: an authenticated user can update email/bio/image and receives 200 with the"
          + " updated values")
  public void ac11_update_current_user_returns_updated_values() {
    String token = jwtService.toToken(user);
    String newEmail = "jane.new@example.com";
    String newBio = "hello world";
    String newImage = "https://example.com/new-avatar.png";

    UserData updated = new UserData(user.getId(), newEmail, username, newBio, newImage);
    when(userReadService.findById(eq(user.getId()))).thenReturn(updated);
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateBody(newEmail, newBio, newImage))
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(newEmail))
        .body("user.bio", equalTo(newBio))
        .body("user.image", equalTo(newImage))
        .body("user.token", equalTo(token));
  }

  @Test
  @DisplayName("AC12: updating to an email already used by another user returns 422")
  public void ac12_update_to_existing_email_returns_422() {
    String token = jwtService.toToken(user);
    String takenEmail = "taken@example.com";

    when(userRepository.findByEmail(eq(takenEmail)))
        .thenReturn(Optional.of(new User(takenEmail, "otheruser", "pw", "", "")));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateBody(takenEmail, "bio", ""))
        .when()
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("email already exist"));
  }

  // ---------------------------------------------------------------------------
  // Token round-trip — AC13
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("AC13: the token issued at login is accepted by a subsequent authenticated request")
  public void ac13_issued_token_is_usable_on_a_follow_up_authenticated_request() {
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    // 1) Log in and capture the issued token (never asserting its exact string).
    String token =
        given()
            .contentType("application/json")
            .body(loginBody(email, password))
            .when()
            .post("/users/login")
            .then()
            .statusCode(200)
            .body("user.token", not(nullValue()))
            .body("user.token", not(equalTo("")))
            .extract()
            .path("user.token");

    // 2) Reuse that token on an authenticated endpoint — it must be accepted.
    given()
        .header("Authorization", "Token " + token)
        .contentType("application/json")
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username));
  }

  // ---------------------------------------------------------------------------
  // Request body builders
  // ---------------------------------------------------------------------------

  private Map<String, Object> registerBody(String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);
    return wrap(user);
  }

  private Map<String, Object> loginBody(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);
    return wrap(user);
  }

  private Map<String, Object> updateBody(String email, String bio, String image) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("bio", bio);
    user.put("image", image);
    return wrap(user);
  }

  private Map<String, Object> wrap(Map<String, Object> user) {
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    return body;
  }
}
