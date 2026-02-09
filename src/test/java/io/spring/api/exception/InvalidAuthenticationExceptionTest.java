package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class InvalidAuthenticationExceptionTest {

  @Test
  public void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    assertThat(exception, notNullValue());
    assertThat(exception.getMessage(), is("invalid email or password"));
  }
}
