package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a comment", "user-123", "article-456");

    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is("This is a comment"));
    assertThat(comment.getUserId(), is("user-123"));
    assertThat(comment.getArticleId(), is("article-456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_generate_unique_id() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");

    assertThat(comment1.getId(), not(comment2.getId()));
  }

  @Test
  public void should_be_equal_by_id() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");

    assertThat(comment1.equals(comment2), is(false));
    assertThat(comment1.equals(comment1), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    Comment comment = new Comment("Test comment", "user-123", "article-456");
    int hashCode1 = comment.hashCode();
    int hashCode2 = comment.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
