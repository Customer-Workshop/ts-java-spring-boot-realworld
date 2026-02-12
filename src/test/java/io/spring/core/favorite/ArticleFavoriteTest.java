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
  public void should_be_equal_when_same_article_and_user() {
    ArticleFavorite fav1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite fav2 = new ArticleFavorite("article-1", "user-1");
    assertThat(fav1.equals(fav2), is(true));
  }

  @Test
  public void should_not_be_equal_when_different_article() {
    ArticleFavorite fav1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite fav2 = new ArticleFavorite("article-2", "user-1");
    assertThat(fav1.equals(fav2), is(false));
  }

  @Test
  public void should_not_be_equal_when_different_user() {
    ArticleFavorite fav1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite fav2 = new ArticleFavorite("article-1", "user-2");
    assertThat(fav1.equals(fav2), is(false));
  }
}
