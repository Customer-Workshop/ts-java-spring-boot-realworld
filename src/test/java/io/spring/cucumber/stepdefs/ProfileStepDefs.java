package io.spring.cucumber.stepdefs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import io.cucumber.java.en.Given;
import io.spring.application.data.UserData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.cucumber.ScenarioContext;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ProfileStepDefs {

  private static final String DEFAULT_AVATAR =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

  /** Tracks which (userId, targetId) pairs represent active follow relationships. */
  private final Set<String> followState = new HashSet<>();

  @Autowired private ScenarioContext context;

  @Autowired private UserRepository userRepository;

  @Autowired private UserReadService userReadService;

  @Autowired private UserRelationshipQueryService userRelationshipQueryService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Given("a user exists with username {string} and email {string}")
  public void aUserExistsWithUsernameAndEmail(String username, String email) {
    User user =
        new User(email, username, passwordEncoder.encode("defaultpass"), "", DEFAULT_AVATAR);
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(user));
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    // Mock the UserReadService for ProfileQueryService lookup
    UserData userData = new UserData(user.getId(), email, username, "", DEFAULT_AVATAR);
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);

    // Dynamic follow state: isUserFollowing checks the in-memory followState set
    when(userRelationshipQueryService.isUserFollowing(any(), eq(user.getId())))
        .thenAnswer(
            invocation -> {
              String userId = invocation.getArgument(0);
              String targetId = invocation.getArgument(1);
              return followState.contains(followKey(userId, targetId));
            });

    // saveRelation adds to followState so subsequent isUserFollowing calls return true
    doAnswer(
            invocation -> {
              FollowRelation rel = invocation.getArgument(0);
              followState.add(followKey(rel.getUserId(), rel.getTargetId()));
              return null;
            })
        .when(userRepository)
        .saveRelation(any(FollowRelation.class));

    // removeRelation removes from followState so subsequent isUserFollowing calls return false
    doAnswer(
            invocation -> {
              FollowRelation rel = invocation.getArgument(0);
              followState.remove(followKey(rel.getUserId(), rel.getTargetId()));
              return null;
            })
        .when(userRepository)
        .removeRelation(any(FollowRelation.class));
  }

  @Given("user {string} is following user {string}")
  public void userIsFollowingUser(String followerUsername, String targetUsername) {
    Optional<User> targetOpt = userRepository.findByUsername(targetUsername);
    Optional<User> followerOpt = userRepository.findByUsername(followerUsername);

    if (targetOpt.isPresent() && followerOpt.isPresent()) {
      User target = targetOpt.get();
      User follower = followerOpt.get();

      // Add follow relationship to state
      followState.add(followKey(follower.getId(), target.getId()));

      // Mock findRelation so unfollow can find and remove it
      FollowRelation relation = new FollowRelation(follower.getId(), target.getId());
      when(userRepository.findRelation(eq(follower.getId()), eq(target.getId())))
          .thenReturn(Optional.of(relation));
    }
  }

  private static String followKey(String userId, String targetId) {
    return userId + "->" + targetId;
  }
}
