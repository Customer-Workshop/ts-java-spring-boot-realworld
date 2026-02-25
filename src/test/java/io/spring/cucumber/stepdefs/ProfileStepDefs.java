package io.spring.cucumber.stepdefs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import io.cucumber.java.en.Given;
import io.spring.application.data.UserData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.cucumber.ScenarioContext;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ProfileStepDefs {

  private static final String DEFAULT_AVATAR =
      "https://static.productionready.io/images/smiley-cyrus.jpg";

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

    // Default: not following
    when(userRelationshipQueryService.isUserFollowing(any(), eq(user.getId()))).thenReturn(false);

    // Allow save/remove relation
    doNothing().when(userRepository).saveRelation(any(FollowRelation.class));
    doNothing().when(userRepository).removeRelation(any(FollowRelation.class));
  }

  @Given("user {string} is following user {string}")
  public void userIsFollowingUser(String followerUsername, String targetUsername) {
    // Look up the target user from the mock
    Optional<User> targetOpt = userRepository.findByUsername(targetUsername);
    Optional<User> followerOpt = userRepository.findByUsername(followerUsername);

    if (targetOpt.isPresent() && followerOpt.isPresent()) {
      User target = targetOpt.get();
      User follower = followerOpt.get();

      // Mock that the follow relation exists
      when(userRelationshipQueryService.isUserFollowing(eq(follower.getId()), eq(target.getId())))
          .thenReturn(true);

      FollowRelation relation = new FollowRelation(follower.getId(), target.getId());
      when(userRepository.findRelation(eq(follower.getId()), eq(target.getId())))
          .thenReturn(Optional.of(relation));

      // After unfollow, should be false
      doNothing().when(userRepository).removeRelation(any(FollowRelation.class));
    }
  }
}
