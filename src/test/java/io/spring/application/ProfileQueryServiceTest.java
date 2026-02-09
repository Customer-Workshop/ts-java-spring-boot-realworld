package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProfileQueryServiceTest {

  @Mock private UserReadService userReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
  }

  @Test
  public void should_find_profile_by_username() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");
    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getUsername(), is("testuser"));
    assertThat(result.get().isFollowing(), is(false));
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    when(userReadService.findByUsername("nonexistent")).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername("nonexistent", null);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_handle_null_current_user() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", null);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFollowing(), is(false));
  }

  @Test
  public void should_show_following_when_user_follows() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    when(userReadService.findByUsername("testuser")).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(true);

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");
    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", currentUser);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFollowing(), is(true));
  }
}
