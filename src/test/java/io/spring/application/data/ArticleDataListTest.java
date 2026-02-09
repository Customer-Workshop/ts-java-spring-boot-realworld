package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_article_data_list() {
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

    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);

    assertThat(list.getArticleDatas().size(), is(1));
    assertThat(list.getCount(), is(1));
  }

  @Test
  public void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertThat(list.getArticleDatas().size(), is(0));
    assertThat(list.getCount(), is(0));
  }

  @Test
  public void should_handle_count_different_from_list_size() {
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

    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 100);

    assertThat(list.getArticleDatas().size(), is(1));
    assertThat(list.getCount(), is(100));
  }
}
