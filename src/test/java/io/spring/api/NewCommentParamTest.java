package io.spring.api;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class NewCommentParamTest {

  @Test
  public void should_create_new_comment_param_with_default_constructor() {
    NewCommentParam param = new NewCommentParam();

    assertThat(param.getBody(), nullValue());
  }
}
