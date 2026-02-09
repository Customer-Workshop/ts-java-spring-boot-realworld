package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  public void should_create_new_article_param() {
    NewArticleParam param = new NewArticleParam("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"));

    assertThat(param.getTitle(), is("Test Title"));
    assertThat(param.getDescription(), is("Test Description"));
    assertThat(param.getBody(), is("Test Body"));
    assertThat(param.getTagList().size(), is(2));
  }

  @Test
  public void should_create_with_builder() {
    NewArticleParam param = NewArticleParam.builder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .tagList(Arrays.asList("java"))
        .build();

    assertThat(param.getTitle(), is("Test Title"));
    assertThat(param.getDescription(), is("Test Description"));
    assertThat(param.getBody(), is("Test Body"));
    assertThat(param.getTagList().size(), is(1));
  }

  @Test
  public void should_create_empty_param() {
    NewArticleParam param = new NewArticleParam();

    assertThat(param.getTitle(), nullValue());
    assertThat(param.getDescription(), nullValue());
    assertThat(param.getBody(), nullValue());
    assertThat(param.getTagList(), nullValue());
  }

  @Test
  public void should_handle_null_tag_list() {
    NewArticleParam param = new NewArticleParam("Test Title", "Test Description", "Test Body", null);

    assertThat(param.getTagList(), nullValue());
  }
}
