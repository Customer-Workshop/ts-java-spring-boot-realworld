package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.PasswordResetService;
import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PasswordResetApi.class)
@Import({WebSecurityConfig.class, BCryptPasswordEncoder.class, JacksonCustomizations.class})
public class PasswordResetApiTest {
  @Autowired private MockMvc mvc;

  @MockBean private PasswordResetService passwordResetService;

  @MockBean private UserRepository userRepository;

  @MockBean private JwtService jwtService;

  @MockBean private UserReadService userReadService;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_request_password_reset_success() {
    String email = "john@jacob.com";

    when(passwordResetService.requestPasswordReset(eq(email))).thenReturn(true);

    Map<String, Object> param = preparePasswordResetRequestParam(email);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset-request")
        .then()
        .statusCode(200)
        .body(
            "passwordReset.message",
            equalTo("If an account exists with this email, a password reset link has been sent."));
  }

  @Test
  public void should_show_error_for_invalid_email_format() {
    String email = "invalid-email";

    Map<String, Object> param = preparePasswordResetRequestParam(email);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset-request")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"));
  }

  @Test
  public void should_show_error_for_blank_email() {
    String email = "";

    Map<String, Object> param = preparePasswordResetRequestParam(email);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset-request")
        .then()
        .statusCode(422);
  }

  @Test
  public void should_reset_password_success() {
    String token = "valid-token";
    String password = "newpassword123";

    when(passwordResetService.resetPassword(eq(token), eq(password))).thenReturn(true);

    Map<String, Object> param = preparePasswordResetParam(token, password);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset")
        .then()
        .statusCode(200)
        .body("passwordReset.message", equalTo("Password has been reset successfully."));
  }

  @Test
  public void should_fail_reset_with_invalid_token() {
    String token = "invalid-token";
    String password = "newpassword123";

    when(passwordResetService.resetPassword(eq(token), eq(password))).thenReturn(false);

    Map<String, Object> param = preparePasswordResetParam(token, password);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset")
        .then()
        .statusCode(400)
        .body("passwordReset.message", equalTo("Invalid or expired reset token."));
  }

  @Test
  public void should_show_error_for_short_password() {
    String token = "valid-token";
    String password = "short";

    Map<String, Object> param = preparePasswordResetParam(token, password);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset")
        .then()
        .statusCode(422)
        .body("errors.password[0]", equalTo("must be at least 8 characters"));
  }

  @Test
  public void should_show_error_for_blank_token() {
    String token = "";
    String password = "newpassword123";

    Map<String, Object> param = preparePasswordResetParam(token, password);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users/password-reset")
        .then()
        .statusCode(422);
  }

  private Map<String, Object> preparePasswordResetRequestParam(String email) {
    return new HashMap<String, Object>() {
      {
        put(
            "passwordReset",
            new HashMap<String, Object>() {
              {
                put("email", email);
              }
            });
      }
    };
  }

  private Map<String, Object> preparePasswordResetParam(String token, String password) {
    return new HashMap<String, Object>() {
      {
        put(
            "passwordReset",
            new HashMap<String, Object>() {
              {
                put("token", token);
                put("password", password);
              }
            });
      }
    };
  }
}
