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
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_handle_punctuation() {
    Article article = new Article("Hello, World!", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("hello-world"));
  }

  @Test
  public void should_handle_slashes() {
    Article article = new Article("TDD/BDD basics", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("tdd-bdd-basics"));
  }

  @Test
  public void should_handle_symbols() {
    Article article =
        new Article("100% coverage #goals", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("100-coverage-goals"));
  }

  @Test
  public void should_collapse_consecutive_separators() {
    Article article =
        new Article("hello --- (world) !!!", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("hello-world"));
  }

  @Test
  public void should_trim_leading_and_trailing_separators() {
    Article article = new Article("!!hello world!!", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("hello-world"));
  }
}
