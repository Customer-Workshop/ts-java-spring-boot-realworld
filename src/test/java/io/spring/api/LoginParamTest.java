package io.spring.api;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class LoginParamTest {

  @Test
  public void should_create_login_param_with_default_constructor() {
    LoginParam loginParam = new LoginParam();

    assertThat(loginParam.getEmail(), nullValue());
    assertThat(loginParam.getPassword(), nullValue());
  }
}
