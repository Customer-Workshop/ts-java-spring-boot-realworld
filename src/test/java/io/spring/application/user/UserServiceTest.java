package io.spring.application.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(passwordEncoder.encode(any(CharSequence.class)))
        .thenAnswer(i -> "encoded_" + i.getArgument(0));
    userService = new UserService(userRepository, "https://default-image.png", passwordEncoder);
  }

  @Test
  public void should_create_user_success() {
    RegisterParam registerParam = new RegisterParam("test@test.com", "testuser", "password");

    User user = userService.createUser(registerParam);

    Assertions.assertNotNull(user);
    Assertions.assertEquals("test@test.com", user.getEmail());
    Assertions.assertEquals("testuser", user.getUsername());
    Assertions.assertEquals("encoded_password", user.getPassword());
    Assertions.assertEquals("https://default-image.png", user.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_create_user_with_empty_bio() {
    RegisterParam registerParam = new RegisterParam("user@email.com", "newuser", "pass123");

    User user = userService.createUser(registerParam);

    Assertions.assertEquals("", user.getBio());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user_email() {
    User existingUser = new User("old@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    Assertions.assertEquals("new@test.com", existingUser.getEmail());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_username() {
    User existingUser = new User("test@test.com", "olduser", "password", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("")
            .username("newuser")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    Assertions.assertEquals("newuser", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_bio_and_image() {
    User existingUser = new User("test@test.com", "testuser", "password", "", "");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("")
            .username("")
            .password("")
            .bio("new bio")
            .image("new-image.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    Assertions.assertEquals("new bio", existingUser.getBio());
    Assertions.assertEquals("new-image.png", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_all_fields() {
    User existingUser = new User("old@test.com", "olduser", "oldpass", "old bio", "old-image.png");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new-image.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    Assertions.assertEquals("new@test.com", existingUser.getEmail());
    Assertions.assertEquals("newuser", existingUser.getUsername());
    Assertions.assertEquals("newpass", existingUser.getPassword());
    Assertions.assertEquals("new bio", existingUser.getBio());
    Assertions.assertEquals("new-image.png", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_not_update_user_fields_when_empty() {
    User existingUser =
        new User("keep@test.com", "keepuser", "keeppass", "keep bio", "keep-image.png");
    UpdateUserParam updateParam =
        UpdateUserParam.builder().email("").username("").password("").bio("").image("").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    Assertions.assertEquals("keep@test.com", existingUser.getEmail());
    Assertions.assertEquals("keepuser", existingUser.getUsername());
    Assertions.assertEquals("keeppass", existingUser.getPassword());
    Assertions.assertEquals("keep bio", existingUser.getBio());
    Assertions.assertEquals("keep-image.png", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_encode_password_when_creating_user() {
    RegisterParam registerParam = new RegisterParam("test@test.com", "testuser", "rawpassword");

    User user = userService.createUser(registerParam);

    Assertions.assertEquals("encoded_rawpassword", user.getPassword());
  }
}
