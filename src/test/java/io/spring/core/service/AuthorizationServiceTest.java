package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_article_author_to_write() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), user.getId());

    boolean result = AuthorizationService.canWriteArticle(user, article);

    assertThat(result, is(true));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    User otherUser = new User("other@example.com", "otheruser", "password", "", "");
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), otherUser.getId());

    boolean result = AuthorizationService.canWriteArticle(user, article);

    assertThat(result, is(false));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    User commenter = new User("commenter@example.com", "commenter", "password", "", "");
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("Test comment", commenter.getId(), article.getId());

    boolean result = AuthorizationService.canWriteComment(user, article, comment);

    assertThat(result, is(true));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@example.com", "author", "password", "", "");
    User commenter = new User("commenter@example.com", "commenter", "password", "", "");
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("Test comment", commenter.getId(), article.getId());

    boolean result = AuthorizationService.canWriteComment(commenter, article, comment);

    assertThat(result, is(true));
  }

  @Test
  public void should_not_allow_non_author_to_write_comment() {
    User articleAuthor = new User("author@example.com", "author", "password", "", "");
    User commenter = new User("commenter@example.com", "commenter", "password", "", "");
    User otherUser = new User("other@example.com", "otheruser", "password", "", "");
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("Test comment", commenter.getId(), article.getId());

    boolean result = AuthorizationService.canWriteComment(otherUser, article, comment);

    assertThat(result, is(false));
  }
}
