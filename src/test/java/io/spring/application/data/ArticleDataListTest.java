package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArticleDataListTest {

  @Test
  void should_create_with_articles_and_count() {
    ArticleData article = new ArticleData();
    article.setId("a1");
    article.setTitle("Test");
    List<ArticleData> articles = Arrays.asList(article);
    ArticleDataList list = new ArticleDataList(articles, 1);

    assertEquals(1, list.getCount());
    assertEquals(1, list.getArticleDatas().size());
    assertEquals("a1", list.getArticleDatas().get(0).getId());
  }

  @Test
  void should_create_with_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertEquals(0, list.getCount());
    assertTrue(list.getArticleDatas().isEmpty());
  }

  @Test
  void should_return_count_independent_of_list_size() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 100);

    assertEquals(100, list.getCount());
    assertTrue(list.getArticleDatas().isEmpty());
  }
}
