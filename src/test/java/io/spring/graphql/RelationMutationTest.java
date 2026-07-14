package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GraphQL parity coverage (AC12) for the Follow / Unfollow Profiles spec (issue #187): the
 * follow/unfollow mutations must set profile.following consistently with the REST endpoints for the
 * same viewer/target, including the unauthenticated and not-found error paths.
 */
@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;

  private User viewer;
  private User target;

  @BeforeEach
  public void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    viewer = new User("viewer@test.com", "viewer", "123", "", "");
    target = new User("target@test.com", "target", "123", "bio", "image");
  }

  private ProfileData profile(boolean following) {
    return new ProfileData(target.getId(), target.getUsername(), "bio", "image", following);
  }

  // AC12: GraphQL follow mutation sets following=true and persists the relation (REST parity, AC5).
  @Test
  public void should_follow_and_set_following_true() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(viewer));
      when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(viewer)))
          .thenReturn(Optional.of(profile(true)));

      ProfilePayload payload = relationMutation.follow(target.getUsername());

      assertTrue(payload.getProfile().getFollowing());
      verify(userRepository).saveRelation(new FollowRelation(viewer.getId(), target.getId()));
    }
  }

  // AC12: GraphQL follow without an authenticated viewer is rejected (REST parity, AC6).
  @Test
  public void should_reject_follow_when_unauthenticated() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> relationMutation.follow(target.getUsername()));
      verify(userRepository, never()).saveRelation(any());
    }
  }

  // AC12: GraphQL follow of a non-existent user is not found (REST parity, AC7).
  @Test
  public void should_reject_follow_of_nonexistent_user() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(viewer));
      when(userRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("ghost"));
      verify(userRepository, never()).saveRelation(any());
    }
  }

  // AC12: GraphQL unfollow of a followed user sets following=false and removes the relation
  // (REST parity, AC9).
  @Test
  public void should_unfollow_and_set_following_false() {
    FollowRelation relation = new FollowRelation(viewer.getId(), target.getId());
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(viewer));
      when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
      when(userRepository.findRelation(eq(viewer.getId()), eq(target.getId())))
          .thenReturn(Optional.of(relation));
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(viewer)))
          .thenReturn(Optional.of(profile(false)));

      ProfilePayload payload = relationMutation.unfollow(target.getUsername());

      assertFalse(payload.getProfile().getFollowing());
      verify(userRepository).removeRelation(relation);
    }
  }

  // AC12: GraphQL unfollow of a not-followed user is an idempotent no-op (REST parity, AC11).
  @Test
  public void should_unfollow_noop_when_not_following() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(viewer));
      when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
      when(userRepository.findRelation(eq(viewer.getId()), eq(target.getId())))
          .thenReturn(Optional.empty());
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(viewer)))
          .thenReturn(Optional.of(profile(false)));

      ProfilePayload payload = relationMutation.unfollow(target.getUsername());

      assertFalse(payload.getProfile().getFollowing());
      verify(userRepository, never()).removeRelation(any());
    }
  }

  // AC12: GraphQL unfollow without an authenticated viewer is rejected (REST parity, AC10).
  @Test
  public void should_reject_unfollow_when_unauthenticated() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> relationMutation.unfollow(target.getUsername()));
      verify(userRepository, never()).removeRelation(any());
    }
  }
}
