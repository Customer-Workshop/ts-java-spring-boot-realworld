package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user-123", "user-456");

    assertThat(relation.getUserId(), is("user-123"));
    assertThat(relation.getTargetId(), is("user-456"));
  }

  @Test
  public void should_be_equal_with_same_fields() {
    FollowRelation relation1 = new FollowRelation("user-123", "user-456");
    FollowRelation relation2 = new FollowRelation("user-123", "user-456");

    assertThat(relation1.equals(relation2), is(true));
  }

  @Test
  public void should_not_be_equal_with_different_user() {
    FollowRelation relation1 = new FollowRelation("user-123", "user-456");
    FollowRelation relation2 = new FollowRelation("user-789", "user-456");

    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_be_equal_with_different_target() {
    FollowRelation relation1 = new FollowRelation("user-123", "user-456");
    FollowRelation relation2 = new FollowRelation("user-123", "user-789");

    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_have_consistent_hashcode() {
    FollowRelation relation = new FollowRelation("user-123", "user-456");
    int hashCode1 = relation.hashCode();
    int hashCode2 = relation.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }

  @Test
  public void should_set_user_id() {
    FollowRelation relation = new FollowRelation("user-123", "user-456");
    relation.setUserId("user-new");

    assertThat(relation.getUserId(), is("user-new"));
  }

  @Test
  public void should_set_target_id() {
    FollowRelation relation = new FollowRelation("user-123", "user-456");
    relation.setTargetId("user-new");

    assertThat(relation.getTargetId(), is("user-new"));
  }
}
