package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class ExceptionsTest {

  @Test
  public void should_create_invalid_request_exception() {
    Errors errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.reject("invalid", "invalid request");

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertThat(exception.getErrors(), notNullValue());
    assertThat(exception.getErrors().hasErrors(), is(true));
  }

  @Test
  public void should_create_resource_not_found_exception() {
    ResourceNotFoundException exception = new ResourceNotFoundException();

    assertThat(exception, notNullValue());
  }

  @Test
  public void should_create_no_authorization_exception() {
    NoAuthorizationException exception = new NoAuthorizationException();

    assertThat(exception, notNullValue());
  }

  @Test
  public void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    assertThat(exception, notNullValue());
  }
}
