package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  public void should_create_update_article_param() {
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Description");

    assertThat(param.getTitle(), is("New Title"));
    assertThat(param.getBody(), is("New Body"));
    assertThat(param.getDescription(), is("New Description"));
  }

  @Test
  public void should_have_default_empty_values() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertThat(param.getTitle(), is(""));
    assertThat(param.getBody(), is(""));
    assertThat(param.getDescription(), is(""));
  }
}
