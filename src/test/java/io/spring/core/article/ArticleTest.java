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
  public void should_keep_plain_title_slug() {
    assertThat(Article.toSlug("How to train your dragon"), is("how-to-train-your-dragon"));
  }

  @Test
  public void should_replace_punctuation() {
    assertThat(Article.toSlug("Hello, World!"), is("hello-world"));
  }

  @Test
  public void should_replace_slashes() {
    assertThat(Article.toSlug("TDD/BDD basics"), is("tdd-bdd-basics"));
  }

  @Test
  public void should_replace_symbols() {
    assertThat(Article.toSlug("100% coverage #goals"), is("100-coverage-goals"));
  }

  @Test
  public void should_collapse_consecutive_separators() {
    assertThat(Article.toSlug("a -- b__c (d)"), is("a-b-c-d"));
  }

  @Test
  public void should_trim_leading_and_trailing_separators() {
    assertThat(Article.toSlug("!!! hello world ???"), is("hello-world"));
  }
}
