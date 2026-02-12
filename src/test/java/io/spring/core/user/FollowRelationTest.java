package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user-1", "user-2");
    assertThat(relation.getUserId(), is("user-1"));
    assertThat(relation.getTargetId(), is("user-2"));
  }

  @Test
  public void should_be_equal_when_same_user_and_target() {
    FollowRelation rel1 = new FollowRelation("user-1", "user-2");
    FollowRelation rel2 = new FollowRelation("user-1", "user-2");
    assertThat(rel1.equals(rel2), is(true));
  }

  @Test
  public void should_not_be_equal_when_different_target() {
    FollowRelation rel1 = new FollowRelation("user-1", "user-2");
    FollowRelation rel2 = new FollowRelation("user-1", "user-3");
    assertThat(rel1.equals(rel2), is(false));
  }

  @Test
  public void should_not_be_equal_when_different_user() {
    FollowRelation rel1 = new FollowRelation("user-1", "user-2");
    FollowRelation rel2 = new FollowRelation("user-3", "user-2");
    assertThat(rel1.equals(rel2), is(false));
  }
}
