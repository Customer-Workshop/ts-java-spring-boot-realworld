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
  public void should_generate_stable_ascii_slug_for_cjk_title() {
    String title = "中文：标题";
    String slug = Article.toSlug(title);

    assertThat(slug.matches("^[a-z0-9-]+$"), is(true));
    assertThat(slug.isEmpty(), is(false));
    assertThat(Article.toSlug(title), is(slug));
  }

  @Test
  public void should_transliterate_accented_latin() {
    assertThat(Article.toSlug("Café Déjà Vu"), is("cafe-deja-vu"));
  }

  @Test
  public void should_strip_emoji_and_symbols() {
    assertThat(Article.toSlug("Hello 🌍 & Java!"), is("hello-java"));
  }

  @Test
  public void should_trim_leading_and_trailing_dashes() {
    assertThat(Article.toSlug("!!! Hello, World !!!"), is("hello-world"));
  }

  @Test
  public void should_generate_fallback_slug_for_empty_titles() {
    assertThat(Article.toSlug(null), is("article-0"));
    assertThat(Article.toSlug("   ").matches("^[a-z0-9-]+$"), is(true));
    assertThat(Article.toSlug("   ").isEmpty(), is(false));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }
}
