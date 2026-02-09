package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class CurrentUserApiTest {

  @Mock
  private UserQueryService userQueryService;

  @Mock
  private UserService userService;

  private CurrentUserApi currentUserApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    currentUserApi = new CurrentUserApi(userQueryService, userService);
  }

  @Test
  public void should_get_current_user() {
    User currentUser = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    UserData userData = new UserData(currentUser.getId(), "test@example.com", "testuser", "bio", "image.jpg");
    when(userQueryService.findById(anyString())).thenReturn(Optional.of(userData));

    ResponseEntity response = currentUserApi.currentUser(currentUser, "Bearer jwt-token-123");

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_update_profile() {
    User currentUser = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    UserData userData = new UserData(currentUser.getId(), "new@example.com", "newuser", "new bio", "new-image.jpg");
    UpdateUserParam updateParam = new UpdateUserParam("new@example.com", "newpassword", "newuser", "new bio", "new-image.jpg");
    when(userService.updateUser(any())).thenReturn(Mono.just(currentUser));
    when(userQueryService.findById(anyString())).thenReturn(Optional.of(userData));

    ResponseEntity response = currentUserApi.updateProfile(currentUser, "Bearer jwt-token-123", updateParam);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }
}
