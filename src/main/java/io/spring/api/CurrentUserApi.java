package io.spring.api;

import io.spring.application.data.UserWithToken;
import io.spring.application.facade.UserApiFacade;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserApiFacade userApiFacade;

  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    String token = authorization.split(" ")[1];
    UserWithToken userWithToken = userApiFacade.getCurrentUser(currentUser, token);
    return ResponseEntity.ok(userResponse(userWithToken));
  }

  @PutMapping
  public ResponseEntity updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    String token = authorization.split(" ")[1];
    UserWithToken userWithToken = userApiFacade.updateUser(currentUser, updateUserParam, token);
    return ResponseEntity.ok(userResponse(userWithToken));
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}
