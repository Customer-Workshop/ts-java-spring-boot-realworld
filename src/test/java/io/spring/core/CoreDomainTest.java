package io.spring.core;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.Util;
import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class CoreDomainTest {

  @Test
  void should_create_user_and_verify_properties() {
    User user = new User("test@mail.com", "testuser", "pass123", "mybio", "myimg");
    assertNotNull(user.getId());
    assertEquals("test@mail.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("pass123", user.getPassword());
    assertEquals("mybio", user.getBio());
    assertEquals("myimg", user.getImage());
  }

  @Test
  void should_update_user_selectively() {
    User user = new User("old@mail.com", "olduser", "oldpass", "oldbio", "oldimg");
    user.update("new@mail.com", "", "", "newbio", "");
    assertEquals("new@mail.com", user.getEmail());
    assertEquals("olduser", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("oldimg", user.getImage());
  }

  @Test
  void should_update_user_all_fields() {
    User user = new User("a@b.com", "u", "p", "b", "i");
    user.update("x@y.com", "nu", "np", "nb", "ni");
    assertEquals("x@y.com", user.getEmail());
    assertEquals("nu", user.getUsername());
    assertEquals("np", user.getPassword());
    assertEquals("nb", user.getBio());
    assertEquals("ni", user.getImage());
  }

  @Test
  void should_test_user_equals_and_hashcode() {
    User u1 = new User("a@b.com", "u", "p", "b", "i");
    User u2 = new User("a@b.com", "u", "p", "b", "i");
    assertEquals(u1, u1);
    assertNotEquals(u1, u2);
    assertNotEquals(u1, null);
    assertNotEquals(u1, "string");
    assertNotEquals(u1.hashCode(), u2.hashCode());
  }

  @Test
  void should_create_article_with_slug() {
    Article article = new Article("Hello World", "desc", "body", Arrays.asList("tag1"), "userId");
    assertEquals("hello-world", article.getSlug());
    assertEquals("Hello World", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals("userId", article.getUserId());
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(1, article.getTags().size());
  }

  @Test
  void should_update_article_fields() {
    Article article = new Article("Original", "desc", "body", Arrays.asList(), "userId");
    article.update("Updated Title", "new desc", "new body");
    assertEquals("updated-title", article.getSlug());
    assertEquals("Updated Title", article.getTitle());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  void should_not_update_article_with_empty_fields() {
    Article article = new Article("Original", "desc", "body", Arrays.asList(), "userId");
    article.update("", "", "");
    assertEquals("original", article.getSlug());
    assertEquals("Original", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  void should_generate_article_slug_correctly() {
    assertEquals("hello-world", Article.toSlug("Hello World"));
    assertEquals("test-article", Article.toSlug("Test Article"));
    assertEquals("special-chars", Article.toSlug("Special & Chars"));
  }

  @Test
  void should_test_article_equals_and_hashcode() {
    Article a1 = new Article("T", "d", "b", Arrays.asList(), "u");
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void should_create_tag() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  void should_create_comment() {
    Comment comment = new Comment("body text", "userId", "articleId");
    assertNotNull(comment.getId());
    assertEquals("body text", comment.getBody());
    assertEquals("userId", comment.getUserId());
    assertEquals("articleId", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_test_comment_equals() {
    Comment c1 = new Comment("body", "uid", "aid");
    assertEquals(c1, c1);
    assertNotEquals(c1, null);
  }

  @Test
  void should_create_article_favorite() {
    ArticleFavorite fav = new ArticleFavorite("artId", "userId");
    assertEquals("artId", fav.getArticleId());
    assertEquals("userId", fav.getUserId());
  }

  @Test
  void should_test_article_favorite_equals() {
    ArticleFavorite f1 = new ArticleFavorite("a", "u");
    ArticleFavorite f2 = new ArticleFavorite("a", "u");
    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
  }

  @Test
  void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("userId", "targetId");
    assertEquals("userId", relation.getUserId());
    assertEquals("targetId", relation.getTargetId());
  }

  @Test
  void should_test_authorization_service_can_write_article() {
    User user = new User("e@t.com", "u", "p", "", "");
    Article article = new Article("T", "d", "b", Arrays.asList(), user.getId());
    assertTrue(AuthorizationService.canWriteArticle(user, article));

    User otherUser = new User("o@t.com", "o", "p", "", "");
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  void should_test_authorization_service_can_write_comment() {
    User articleOwner = new User("e@t.com", "owner", "p", "", "");
    User commenter = new User("c@t.com", "commenter", "p", "", "");
    User stranger = new User("s@t.com", "stranger", "p", "", "");

    Article article = new Article("T", "d", "b", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("text", commenter.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleOwner, article, comment));
    assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
    assertFalse(AuthorizationService.canWriteComment(stranger, article, comment));
  }

  @Test
  void should_test_util_is_empty() {
    assertTrue(Util.isEmpty(null));
    assertTrue(Util.isEmpty(""));
    assertFalse(Util.isEmpty("hello"));
    assertFalse(Util.isEmpty(" "));
  }
}
