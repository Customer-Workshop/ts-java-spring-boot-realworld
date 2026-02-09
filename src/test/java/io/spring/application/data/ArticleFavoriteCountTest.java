package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-1", 10);

    assertThat(count.getId(), is("article-1"));
    assertThat(count.getCount(), is(10));
  }

  @Test
  public void should_be_equal_with_same_values() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article-1", 10);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article-1", 10);

    assertThat(count1.equals(count2), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-1", 10);
    int hashCode1 = count.hashCode();
    int hashCode2 = count.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }

  @Test
  public void should_handle_zero_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-1", 0);

    assertThat(count.getCount(), is(0));
  }
}
