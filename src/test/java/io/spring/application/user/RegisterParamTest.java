package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  public void should_create_register_param() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password123");

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getPassword(), is("password123"));
  }

  @Test
  public void should_create_empty_register_param() {
    RegisterParam param = new RegisterParam();

    assertThat(param.getEmail() == null, is(true));
    assertThat(param.getUsername() == null, is(true));
    assertThat(param.getPassword() == null, is(true));
  }
}
