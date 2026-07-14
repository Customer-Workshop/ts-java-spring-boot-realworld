package io.spring.infrastructure.user;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Persistence read-model coverage for the Follow / Unfollow Profiles spec (issue #187), AC13: the
 * follow-relation read model reports following=true only for the exact (follower, followee) pair,
 * not the reverse.
 */
@Import(MyBatisUserRepository.class)
public class UserRelationshipQueryServiceTest extends DbTestBase {
  @Autowired private UserRepository userRepository;
  @Autowired private UserRelationshipQueryService userRelationshipQueryService;

  private User follower;
  private User followee;

  @BeforeEach
  public void setUp() {
    follower = new User("follower@test.com", "follower", "123", "", "");
    followee = new User("followee@test.com", "followee", "123", "", "");
    userRepository.save(follower);
    userRepository.save(followee);
  }

  // AC13: following is directional — only the exact (follower, followee) pair reports true.
  @Test
  public void should_report_following_only_for_exact_pair_not_reverse() {
    userRepository.saveRelation(new FollowRelation(follower.getId(), followee.getId()));

    Assertions.assertTrue(
        userRelationshipQueryService.isUserFollowing(follower.getId(), followee.getId()));
    Assertions.assertFalse(
        userRelationshipQueryService.isUserFollowing(followee.getId(), follower.getId()));
  }

  // AC13: an unrelated pair reports following=false.
  @Test
  public void should_report_not_following_for_unrelated_pair() {
    Assertions.assertFalse(
        userRelationshipQueryService.isUserFollowing(follower.getId(), followee.getId()));
  }
}
