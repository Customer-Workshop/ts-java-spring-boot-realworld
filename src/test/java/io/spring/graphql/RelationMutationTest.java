package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
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

@ExtendWith(MockitoExtension.class)
class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;

  @BeforeEach
  void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User user) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_follow_user() {
    User currentUser = new User("a@b.com", "user1", "pass", "", "");
    User target = new User("c@d.com", "target", "pass", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    ProfileData profileData = new ProfileData(target.getId(), "target", "", "", true);
    when(profileQueryService.findByUsername("target", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("target");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("target", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void should_throw_when_not_authenticated_for_follow() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anon",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> relationMutation.follow("target"));
  }

  @Test
  void should_throw_when_target_not_found_for_follow() {
    User currentUser = new User("a@b.com", "user1", "pass", "", "");
    authenticateUser(currentUser);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void should_unfollow_user() {
    User currentUser = new User("a@b.com", "user1", "pass", "", "");
    User target = new User("c@d.com", "target", "pass", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    FollowRelation relation = new FollowRelation(currentUser.getId(), target.getId());
    when(userRepository.findRelation(currentUser.getId(), target.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(target.getId(), "target", "", "", false);
    when(profileQueryService.findByUsername("target", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("target");

    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  void should_throw_when_not_authenticated_for_unfollow() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anon",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("target"));
  }

  @Test
  void should_throw_when_relation_not_found_for_unfollow() {
    User currentUser = new User("a@b.com", "user1", "pass", "", "");
    User target = new User("c@d.com", "target", "pass", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("target")).thenReturn(Optional.of(target));
    when(userRepository.findRelation(currentUser.getId(), target.getId()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("target"));
  }
}
