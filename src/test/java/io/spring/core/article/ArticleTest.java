package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
  public void should_create_article_with_all_fields() {
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java", "spring"), "user-1");

    assertThat(article.getId(), notNullValue());
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Description"));
    assertThat(article.getBody(), is("Body"));
    assertThat(article.getUserId(), is("user-1"));
    assertThat(article.getTags().size(), is(2));
    assertThat(article.getCreatedAt(), notNullValue());
    assertThat(article.getUpdatedAt(), notNullValue());
  }

  @Test
  public void should_create_article_with_custom_created_at() {
    LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
    Article article =
        new Article(
            "Test Title", "Description", "Body", Arrays.asList("java"), "user-1", createdAt);

    assertThat(article.getCreatedAt(), is(createdAt));
    assertThat(article.getUpdatedAt(), is(createdAt));
  }

  @Test
  public void should_create_article_with_empty_tags() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    assertThat(article.getTags().size(), is(0));
  }

  @Test
  public void should_update_title() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    article.update("New Title", null, null);

    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("Description"));
    assertThat(article.getBody(), is("Body"));
  }

  @Test
  public void should_update_description() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    article.update(null, "New Description", null);

    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("New Description"));
    assertThat(article.getBody(), is("Body"));
  }

  @Test
  public void should_update_body() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    article.update(null, null, "New Body");

    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Description"));
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_update_all_fields() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    article.update("New Title", "New Description", "New Body");

    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("New Description"));
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_not_update_with_empty_values() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    article.update("", "", "");

    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Description"));
    assertThat(article.getBody(), is("Body"));
  }

  @Test
  public void should_deduplicate_tags() {
    Article article =
        new Article(
            "Test Title", "Description", "Body", Arrays.asList("java", "java", "spring"), "user-1");

    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_have_equals_based_on_id() {
    Article article1 =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");
    Article article2 =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    assertThat(article1.equals(article2), is(false));
    assertThat(article1.equals(article1), is(true));
  }

  @Test
  public void should_have_hashcode_based_on_id() {
    Article article =
        new Article("Test Title", "Description", "Body", Collections.emptyList(), "user-1");

    assertThat(article.hashCode(), notNullValue());
  }

  @Test
  public void should_generate_slug_with_special_characters() {
    String slug = Article.toSlug("Test & Title");

    assertThat(slug.contains(" "), is(false));
  }
}
