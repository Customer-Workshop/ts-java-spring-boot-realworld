package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

  @Test
  void should_be_runtime_exception() {
    AuthenticationException exception = new AuthenticationException();
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void should_create_without_message() {
    AuthenticationException exception = new AuthenticationException();
    assertNull(exception.getMessage());
  }
}
