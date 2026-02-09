package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_find_user_by_id() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    when(userReadService.findById("user-1")).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById("user-1");

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getUsername(), is("testuser"));
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findById("nonexistent")).thenReturn(null);

    Optional<UserData> result = userQueryService.findById("nonexistent");

    assertThat(result.isPresent(), is(false));
  }
}
