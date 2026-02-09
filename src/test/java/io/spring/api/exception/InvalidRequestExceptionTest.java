package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class InvalidRequestExceptionTest {

  @Test
  public void should_create_invalid_request_exception_with_binding_result() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("user", "email", "can't be empty");
    when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

    InvalidRequestException exception = new InvalidRequestException(bindingResult);

    assertThat(exception, notNullValue());
    assertThat(exception.getErrors(), is(bindingResult));
  }
}
