package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommentTest {

  @Test
  void should_create_comment_with_constructor() {
    Comment comment = new Comment("test body", "user1", "article1");

    assertNotNull(comment.getId());
    assertEquals("test body", comment.getBody());
    assertEquals("user1", comment.getUserId());
    assertEquals("article1", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_generate_unique_ids() {
    Comment c1 = new Comment("body1", "u1", "a1");
    Comment c2 = new Comment("body2", "u2", "a2");

    assertNotEquals(c1.getId(), c2.getId());
  }

  @Test
  void should_support_equals_based_on_id() {
    Comment c1 = new Comment("body", "u1", "a1");
    Comment c2 = new Comment("body", "u1", "a1");

    assertNotEquals(c1, c2);
    assertEquals(c1, c1);
  }

  @Test
  void should_create_with_no_arg_constructor() {
    Comment comment = new Comment();
    assertNull(comment.getId());
    assertNull(comment.getBody());
  }
}
