package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_article_data() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            5,
            now,
            now,
            Arrays.asList("java", "spring"),
            profile);

    assertThat(article.getId(), is("article-1"));
    assertThat(article.getSlug(), is("test-slug"));
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Test Description"));
    assertThat(article.getBody(), is("Test Body"));
    assertThat(article.isFavorited(), is(false));
    assertThat(article.getFavoritesCount(), is(5));
    assertThat(article.getTagList().size(), is(2));
    assertThat(article.getProfileData(), notNullValue());
  }

  @Test
  public void should_get_cursor() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertThat(article.getCursor(), notNullValue());
    assertThat(article.getCursor().getData(), is(now));
  }

  @Test
  public void should_set_favorited() {
    ArticleData article = new ArticleData();
    article.setFavorited(true);

    assertThat(article.isFavorited(), is(true));
  }

  @Test
  public void should_set_favorites_count() {
    ArticleData article = new ArticleData();
    article.setFavoritesCount(10);

    assertThat(article.getFavoritesCount(), is(10));
  }

  @Test
  public void should_set_tag_list() {
    ArticleData article = new ArticleData();
    article.setTagList(Arrays.asList("java", "spring", "boot"));

    assertThat(article.getTagList().size(), is(3));
  }
}
