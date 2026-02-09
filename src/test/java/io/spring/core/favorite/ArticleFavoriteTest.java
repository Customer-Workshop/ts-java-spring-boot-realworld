package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("article-123", "user-456");

    assertThat(favorite.getArticleId(), is("article-123"));
    assertThat(favorite.getUserId(), is("user-456"));
  }

  @Test
  public void should_be_equal_with_same_fields() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-123", "user-456");

    assertThat(favorite1.equals(favorite2), is(true));
  }

  @Test
  public void should_not_be_equal_with_different_article() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-789", "user-456");

    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_be_equal_with_different_user() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-123", "user-456");
    ArticleFavorite favorite2 = new ArticleFavorite("article-123", "user-789");

    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_have_consistent_hashcode() {
    ArticleFavorite favorite = new ArticleFavorite("article-123", "user-456");
    int hashCode1 = favorite.hashCode();
    int hashCode2 = favorite.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
