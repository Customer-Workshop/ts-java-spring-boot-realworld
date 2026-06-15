package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class RelationMutationTest {

  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;
  private RelationMutation relationMutation;
  private User user;
  private User targetUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    profileQueryService = mock(ProfileQueryService.class);
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@test.com", "targetuser", "password", "bio", "img");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_follow_user_successfully() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData profileData = new ProfileData(targetUser.getId(), "targetuser", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("targetuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_when_following_without_auth() {
    AnonymousAuthenticationToken anon =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("someone"));
  }

  @Test
  void should_throw_when_following_nonexistent_user() {
    when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nobody"));
  }

  @Test
  void should_unfollow_user_successfully() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    FollowRelation relation = new FollowRelation(user.getId(), targetUser.getId());
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData =
        new ProfileData(targetUser.getId(), "targetuser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("targetuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertFalse(result.getProfile().getFollowing());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_throw_when_unfollowing_without_auth() {
    AnonymousAuthenticationToken anon =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("someone"));
  }

  @Test
  void should_throw_when_unfollowing_nonexistent_user() {
    when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nobody"));
  }

  @Test
  void should_throw_when_no_relation_exists_for_unfollow() {
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
