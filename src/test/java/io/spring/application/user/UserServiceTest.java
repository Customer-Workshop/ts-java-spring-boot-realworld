package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UserService(userRepository, "default-image.jpg", passwordEncoder);
  }

  @Test
  public void should_create_user() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password123");
    User savedUser =
        new User("test@example.com", "testuser", "encoded-password", "", "default-image.jpg");
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

    User result = userService.createUser(param).block();

    assertThat(result, notNullValue());
    assertThat(result.getEmail(), is("test@example.com"));
    assertThat(result.getUsername(), is("testuser"));
  }

  @Test
  public void should_update_user() {
    User existingUser =
        new User("old@example.com", "olduser", "password", "old bio", "old-image.jpg");
    UpdateUserParam param =
        new UpdateUserParam(
            "new@example.com", "newpassword", "newuser", "new bio", "new-image.jpg");
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(existingUser));

    User result = userService.updateUser(command).block();

    assertThat(result, notNullValue());
  }

  @Test
  public void should_check_email_unique_when_not_exists() {
    when(userRepository.findByEmail("new@example.com")).thenReturn(Mono.empty());

    Boolean result = userService.checkEmailUnique("new@example.com", null).block();

    assertThat(result, is(true));
  }

  @Test
  public void should_check_email_unique_when_same_user() {
    User currentUser = new User("test@example.com", "testuser", "password", "", "");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(currentUser));

    Boolean result = userService.checkEmailUnique("test@example.com", currentUser).block();

    assertThat(result, is(true));
  }

  @Test
  public void should_check_username_unique_when_not_exists() {
    when(userRepository.findByUsername("newuser")).thenReturn(Mono.empty());

    Boolean result = userService.checkUsernameUnique("newuser", null).block();

    assertThat(result, is(true));
  }

  @Test
  public void should_check_username_unique_when_same_user() {
    User currentUser = new User("test@example.com", "testuser", "password", "", "");
    when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(currentUser));

    Boolean result = userService.checkUsernameUnique("testuser", currentUser).block();

    assertThat(result, is(true));
  }
}
