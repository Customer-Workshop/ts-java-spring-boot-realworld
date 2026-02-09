package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_comment_data() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment = new CommentData("comment-1", "This is a comment", "article-1", now, now, profile);

    assertThat(comment.getId(), is("comment-1"));
    assertThat(comment.getBody(), is("This is a comment"));
    assertThat(comment.getArticleId(), is("article-1"));
    assertThat(comment.getCreatedAt(), is(now));
    assertThat(comment.getUpdatedAt(), is(now));
    assertThat(comment.getProfileData(), notNullValue());
  }

  @Test
  public void should_get_cursor() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment = new CommentData("comment-1", "This is a comment", "article-1", now, now, profile);

    assertThat(comment.getCursor(), notNullValue());
    assertThat(comment.getCursor().getData(), is(now));
  }

  @Test
  public void should_be_equal_with_same_values() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment1 = new CommentData("comment-1", "This is a comment", "article-1", now, now, profile);
    CommentData comment2 = new CommentData("comment-1", "This is a comment", "article-1", now, now, profile);

    assertThat(comment1.equals(comment2), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment = new CommentData("comment-1", "This is a comment", "article-1", now, now, profile);
    int hashCode1 = comment.hashCode();
    int hashCode2 = comment.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
