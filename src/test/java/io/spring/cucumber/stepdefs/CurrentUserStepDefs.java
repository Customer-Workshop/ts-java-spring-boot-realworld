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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CurrentUserStepDefs {

  private static final String DEFAULT_AVATAR =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

  @Autowired private ScenarioContext context;

  @Autowired private UserRepository userRepository;

  @Autowired private JwtService jwtService;

  @Autowired private UserReadService userReadService;

  @Autowired private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Given("I am authenticated as user {string} with username {string} and password {string}")
  public void iAmAuthenticatedAsUser(String email, String username, String password) {
    User user = new User(email, username, passwordEncoder.encode(password), "", DEFAULT_AVATAR);
    UserData userData = new UserData(user.getId(), email, username, "", DEFAULT_AVATAR);
    String token = "test-jwt-token-" + username;

    when(jwtService.toToken(any())).thenReturn(token);
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(user));
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);

    context.setAuthToken(token);
    context.setAuthenticatedUserId(user.getId());
    context.setAuthenticatedUsername(username);
  }

  @Given("I have an update user payload with bio {string}")
  public void iHaveAnUpdateUserPayloadWithBio(String bio) {
    Map<String, String> fields = new HashMap<>();
    fields.put("bio", bio);
    context.setRequestBody(PayloadBuilder.updateUserPayload(fields));

    // Update mock to return the updated data
    updateMockUserData(null, null, bio, null);
  }

  @Given("I have an update user payload with image {string}")
  public void iHaveAnUpdateUserPayloadWithImage(String image) {
    Map<String, String> fields = new HashMap<>();
    fields.put("image", image);
    context.setRequestBody(PayloadBuilder.updateUserPayload(fields));

    updateMockUserData(null, null, null, image);
  }

  @Given("I have an update user payload with email {string}")
  public void iHaveAnUpdateUserPayloadWithEmail(String email) {
    Map<String, String> fields = new HashMap<>();
    fields.put("email", email);
    context.setRequestBody(PayloadBuilder.updateUserPayload(fields));

    updateMockUserData(email, null, null, null);
  }

  @Given("I have an update user payload with a bio of {int} characters")
  public void iHaveAnUpdateUserPayloadWithLongBio(int length) {
    String longBio = PayloadBuilder.generateString(length, 'A');
    Map<String, String> fields = new HashMap<>();
    fields.put("bio", longBio);
    context.setRequestBody(PayloadBuilder.updateUserPayload(fields));

    updateMockUserData(null, null, longBio, null);
  }

  private void updateMockUserData(String email, String username, String bio, String image) {
    // Re-read the authenticated user's data and produce an updated version
    String userId = context.getAuthenticatedUserId();
    String authUsername = context.getAuthenticatedUsername();
    if (userId == null) {
      return;
    }

    String finalEmail = email != null ? email : "currentuser@example.com";
    String finalUsername = username != null ? username : authUsername;
    String finalBio = bio != null ? bio : "";
    String finalImage = image != null ? image : DEFAULT_AVATAR;

    UserData updatedData = new UserData(userId, finalEmail, finalUsername, finalBio, finalImage);
    when(userReadService.findById(eq(userId))).thenReturn(updatedData);
  }
}
