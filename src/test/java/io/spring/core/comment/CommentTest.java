package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a comment", "user-123", "article-456");
    assertThat(comment.getBody(), is("This is a comment"));
    assertThat(comment.getUserId(), is("user-123"));
    assertThat(comment.getArticleId(), is("article-456"));
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_generate_unique_ids_for_comments() {
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article2");
    assertThat(comment1.getId().equals(comment2.getId()), is(false));
  }

  @Test
  public void should_have_equality_based_on_id() {
    Comment comment1 = new Comment("body", "user1", "article1");
    Comment comment2 = new Comment("body", "user1", "article1");
    assertThat(comment1.equals(comment2), is(false));
    assertThat(comment1.equals(comment1), is(true));
  }
}
