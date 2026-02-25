package io.spring.cucumber.stepdefs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.cucumber.java.en.Given;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.cucumber.PayloadBuilder;
import io.spring.cucumber.ScenarioContext;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserLoginStepDefs {

  private static final String DEFAULT_AVATAR =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

  @Autowired private ScenarioContext context;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Autowired private UserReadService userReadService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Given("a registered user exists with email {string}, username {string}, and password {string}")
  public void aRegisteredUserExists(String email, String username, String password) {
    User user = new User(email, username, passwordEncoder.encode(password), "", DEFAULT_AVATAR);
    UserData userData = new UserData(user.getId(), email, username, "", DEFAULT_AVATAR);

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(jwtService.toToken(any())).thenReturn("test-jwt-token");
  }

  @Given("I have a login payload with email {string} and password {string}")
  public void iHaveALoginPayload(String email, String password) {
    context.setRequestBody(PayloadBuilder.loginPayload(email, password));
  }
}
