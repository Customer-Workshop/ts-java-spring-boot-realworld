package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GraphQL parity coverage for reading a profile's follow state (issue #187): the GraphQL {@code
 * profile} query must expose {@code following} consistently with the REST {@code GET
 * /profiles/{username}} endpoint (AC2, AC3) and 404 semantics (AC4).
 */
@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ProfileDatafetcher profileDatafetcher;
  private User viewer;
  private User target;

  @BeforeEach
  public void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    viewer = new User("viewer@test.com", "viewer", "123", "", "");
    target = new User("target@test.com", "target", "123", "bio", "image");
    when(dataFetchingEnvironment.getArgument("username")).thenReturn(target.getUsername());
  }

  private ProfileData profile(boolean following) {
    return new ProfileData(target.getId(), target.getUsername(), "bio", "image", following);
  }

  // AC2 (GraphQL parity): an authenticated follower sees following=true.
  @Test
  public void should_expose_following_true_for_authenticated_follower() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(viewer));
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(viewer)))
          .thenReturn(Optional.of(profile(true)));

      ProfilePayload payload =
          profileDatafetcher.queryProfile(target.getUsername(), dataFetchingEnvironment);

      assertEquals(target.getUsername(), payload.getProfile().getUsername());
      assertTrue(payload.getProfile().getFollowing());
    }
  }

  // AC3 (GraphQL parity): an anonymous viewer always sees following=false.
  @Test
  public void should_expose_following_false_for_anonymous_viewer() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(null)))
          .thenReturn(Optional.of(profile(false)));

      ProfilePayload payload =
          profileDatafetcher.queryProfile(target.getUsername(), dataFetchingEnvironment);

      assertFalse(payload.getProfile().getFollowing());
    }
  }

  // AC4 (GraphQL parity): querying a non-existent profile is not found.
  @Test
  public void should_throw_not_found_for_unknown_profile() {
    try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(profileQueryService.findByUsername(eq(target.getUsername()), eq(null)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> profileDatafetcher.queryProfile(target.getUsername(), dataFetchingEnvironment));
    }
  }
}
