package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserService userService;

  private UserMutation userMutation;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@email.com")
            .username("testuser")
            .password("password123")
            .build();
    User user = new User("test@email.com", "testuser", "encoded", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_login_successfully() {
    User user = new User("test@email.com", "testuser", "encoded", "", "");
    when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "encoded")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@email.com");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void should_throw_on_invalid_login() {
    when(userRepository.findByEmail("wrong@email.com")).thenReturn(Optional.empty());

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("password", "wrong@email.com"));
  }

  @Test
  void should_throw_on_wrong_password() {
    User user = new User("test@email.com", "testuser", "encoded", "", "");
    when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(
        InvalidAuthenticationException.class,
        () -> userMutation.login("wrong", "test@email.com"));
  }

  @Test
  void should_update_user_when_authenticated() {
    User currentUser = new User("test@email.com", "testuser", "pass", "bio", "img");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .username("newuser")
            .email("new@email.com")
            .bio("new bio")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }

  @Test
  void should_return_null_when_anonymous_update() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anon",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    UpdateUserInput input = UpdateUserInput.newBuilder().username("newuser").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }
}
