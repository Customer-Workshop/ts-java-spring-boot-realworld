package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FollowRelationTest {

  @Test
  void should_create_with_constructor() {
    FollowRelation relation = new FollowRelation("user1", "user2");

    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  void should_create_with_no_arg_constructor() {
    FollowRelation relation = new FollowRelation();
    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  void should_support_setters() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("u1");
    relation.setTargetId("t1");

    assertEquals("u1", relation.getUserId());
    assertEquals("t1", relation.getTargetId());
  }

  @Test
  void should_support_equals_and_hashCode() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u1", "t1");

    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void should_not_equal_different_relations() {
    FollowRelation r1 = new FollowRelation("u1", "t1");
    FollowRelation r2 = new FollowRelation("u1", "t2");

    assertNotEquals(r1, r2);
  }

  @Test
  void should_support_toString() {
    FollowRelation relation = new FollowRelation("u1", "t1");
    assertNotNull(relation.toString());
    assertTrue(relation.toString().contains("u1"));
  }
}
