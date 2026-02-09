package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class ProfileApiTest {

  @Mock private ProfileQueryService profileQueryService;

  @Mock private UserRepository userRepository;

  private ProfileApi profileApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    profileApi = new ProfileApi(profileQueryService, userRepository);
  }

  @Test
  public void should_get_profile() {
    ProfileData profileData = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    when(profileQueryService.findByUsername(anyString(), any()))
        .thenReturn(Optional.of(profileData));

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");
    ResponseEntity response = profileApi.getProfile("testuser", currentUser);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_throw_not_found_when_profile_not_exists() {
    when(profileQueryService.findByUsername(anyString(), any())).thenReturn(Optional.empty());

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          profileApi.getProfile("nonexistent", currentUser);
        });
  }

  @Test
  public void should_follow_user() {
    User targetUser = new User("target@example.com", "targetuser", "password", "", "");
    ProfileData profileData = new ProfileData("user-1", "targetuser", "bio", "image.jpg", true);
    when(userRepository.findByUsername("targetuser")).thenReturn(Mono.just(targetUser));
    when(userRepository.saveRelation(any(FollowRelation.class))).thenReturn(Mono.empty());
    when(profileQueryService.findByUsername(anyString(), any()))
        .thenReturn(Optional.of(profileData));

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");
    ResponseEntity response = profileApi.follow("targetuser", currentUser);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  public void should_throw_not_found_when_follow_nonexistent_user() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          profileApi.follow("nonexistent", currentUser);
        });
  }

  @Test
  public void should_unfollow_user() {
    User targetUser = new User("target@example.com", "targetuser", "password", "", "");
    FollowRelation relation = new FollowRelation("current-id", "target-id");
    ProfileData profileData = new ProfileData("user-1", "targetuser", "bio", "image.jpg", false);
    when(userRepository.findByUsername("targetuser")).thenReturn(Mono.just(targetUser));
    when(userRepository.findRelation(anyString(), anyString())).thenReturn(Mono.just(relation));
    when(userRepository.removeRelation(any(FollowRelation.class))).thenReturn(Mono.empty());
    when(profileQueryService.findByUsername(anyString(), any()))
        .thenReturn(Optional.of(profileData));

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");
    ResponseEntity response = profileApi.unfollow("targetuser", currentUser);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  public void should_throw_not_found_when_unfollow_nonexistent_user() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Mono.empty());

    User currentUser = new User("current@example.com", "currentuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          profileApi.unfollow("nonexistent", currentUser);
        });
  }
}
