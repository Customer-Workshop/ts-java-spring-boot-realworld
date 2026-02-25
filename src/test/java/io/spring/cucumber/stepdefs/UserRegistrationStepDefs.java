package io.spring.cucumber.stepdefs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.cucumber.java.en.Given;
import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.cucumber.PayloadBuilder;
import io.spring.cucumber.ScenarioContext;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRegistrationStepDefs {

  private static final String DEFAULT_AVATAR =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

  @Autowired private ScenarioContext context;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Autowired private UserReadService userReadService;

  @Autowired private UserService userService;

  @Given(
      "I have a registration payload with email {string}, username {string}, and password {string}")
  public void iHaveARegistrationPayload(String email, String username, String password) {
    // Set up mocks for successful registration
    when(jwtService.toToken(any())).thenReturn("test-jwt-token");
    User user = new User(email, username, password, "", DEFAULT_AVATAR);
    UserData userData = new UserData(user.getId(), email, username, "", DEFAULT_AVATAR);
    when(userReadService.findById(any())).thenReturn(userData);
    when(userService.createUser(any())).thenReturn(user);

    // Default: no duplicates
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    context.setRequestBody(PayloadBuilder.registrationPayload(email, username, password));
  }

  @Given("a user already exists with email {string} and username {string}")
  public void aUserAlreadyExistsWithEmailAndUsername(String email, String username) {
    User existingUser = new User(email, username, "existingpass", "bio", DEFAULT_AVATAR);
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(existingUser));
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(existingUser));
  }

  @Given("I have a registration payload without email")
  public void iHaveARegistrationPayloadWithoutEmail() {
    context.setRequestBody(PayloadBuilder.registrationPayloadWithout("email"));
  }

  @Given("I have a registration payload without username")
  public void iHaveARegistrationPayloadWithoutUsername() {
    context.setRequestBody(PayloadBuilder.registrationPayloadWithout("username"));
  }

  @Given("I have a registration payload without password")
  public void iHaveARegistrationPayloadWithoutPassword() {
    context.setRequestBody(PayloadBuilder.registrationPayloadWithout("password"));
  }
}
