package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.application.PasswordResetService;
import io.spring.application.data.PasswordResetData;
import io.spring.application.data.PasswordResetRequestData;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PasswordResetApi {
  private PasswordResetService passwordResetService;

  @RequestMapping(path = "/users/password-reset-request", method = POST)
  public ResponseEntity<Map<String, Object>> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequestParam param) {
    passwordResetService.requestPasswordReset(param.getEmail());
    return ResponseEntity.ok(
        response(
            new PasswordResetRequestData(
                "If an account exists with this email, a password reset link has been sent.")));
  }

  @RequestMapping(path = "/users/password-reset", method = POST)
  public ResponseEntity<Map<String, Object>> resetPassword(
      @Valid @RequestBody PasswordResetParam param) {
    boolean success = passwordResetService.resetPassword(param.getToken(), param.getPassword());
    if (success) {
      return ResponseEntity.ok(
          response(new PasswordResetData("Password has been reset successfully.")));
    } else {
      return ResponseEntity.badRequest()
          .body(response(new PasswordResetData("Invalid or expired reset token.")));
    }
  }

  private Map<String, Object> response(Object data) {
    return new HashMap<String, Object>() {
      {
        put("passwordReset", data);
      }
    };
  }
}

@Getter
@JsonRootName("passwordReset")
@NoArgsConstructor
class PasswordResetRequestParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;
}

@Getter
@JsonRootName("passwordReset")
@NoArgsConstructor
class PasswordResetParam {
  @NotBlank(message = "can't be empty")
  private String token;

  @NotBlank(message = "can't be empty")
  @Size(min = 8, message = "must be at least 8 characters")
  private String password;
}
