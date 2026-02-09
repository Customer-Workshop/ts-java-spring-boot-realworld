package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CommentDataListTest {

  @Test
  public void should_create_comment_data_list_with_comments() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment =
        new CommentData("comment-1", "Test comment", "article-1", now, now, profile);

    assertThat(comment, notNullValue());
    assertThat(comment.getId(), is("comment-1"));
    assertThat(comment.getBody(), is("Test comment"));
    assertThat(comment.getArticleId(), is("article-1"));
    assertThat(comment.getCreatedAt(), is(now));
    assertThat(comment.getUpdatedAt(), is(now));
    assertThat(comment.getProfileData(), is(profile));
  }
}
