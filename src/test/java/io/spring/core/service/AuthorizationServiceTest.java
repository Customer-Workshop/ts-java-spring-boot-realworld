package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AuthorizationServiceTest {

  @Test
  void should_allow_article_owner_to_write() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("tag"), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void should_deny_non_owner_to_write_article() {
    User owner = new User("a@b.com", "owner", "pass", "", "");
    User other = new User("c@d.com", "other", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("tag"), owner.getId());

    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  void should_allow_article_owner_to_write_comment() {
    User articleOwner = new User("a@b.com", "owner", "pass", "", "");
    User commentAuthor = new User("c@d.com", "commenter", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("tag"), articleOwner.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleOwner, article, comment));
  }

  @Test
  void should_allow_comment_author_to_write_comment() {
    User articleOwner = new User("a@b.com", "owner", "pass", "", "");
    User commentAuthor = new User("c@d.com", "commenter", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("tag"), articleOwner.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  void should_deny_unrelated_user_to_write_comment() {
    User articleOwner = new User("a@b.com", "owner", "pass", "", "");
    User commentAuthor = new User("c@d.com", "commenter", "pass", "", "");
    User unrelated = new User("e@f.com", "unrelated", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("tag"), articleOwner.getId());
    Comment comment = new Comment("body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(unrelated, article, comment));
  }
}
