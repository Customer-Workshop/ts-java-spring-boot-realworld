package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_strip_non_ascii_characters() {
    Article article =
        new Article("中文：标题 hello world", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("hello-world"));
  }

  @Test
  public void should_transliterate_accented_characters() {
    Article article = new Article("Café Résumé", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("cafe-resume"));
  }

  @Test
  public void should_fall_back_to_deterministic_hash_when_title_has_no_ascii() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is(Article.toSlug("中文：标题")));
    assertThat(article.getSlug().matches("[a-f0-9]{12}"), is(true));
  }

  @Test
  public void should_generate_same_slug_for_same_non_ascii_title() {
    Article first = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    Article second = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "456");
    assertThat(first.getSlug(), is(second.getSlug()));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }
}
