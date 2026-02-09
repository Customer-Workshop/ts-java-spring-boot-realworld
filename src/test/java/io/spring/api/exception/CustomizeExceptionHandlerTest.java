package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  public void should_handle_invalid_request_exception() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("user", "email", "can't be empty");
    when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
    InvalidRequestException exception = new InvalidRequestException(bindingResult);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_handle_constraint_violation_exception_with_empty_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource response = handler.handleConstraintViolation(exception, request);

    assertThat(response, notNullValue());
    assertThat(response.getFieldErrors().size(), is(0));
  }
}
