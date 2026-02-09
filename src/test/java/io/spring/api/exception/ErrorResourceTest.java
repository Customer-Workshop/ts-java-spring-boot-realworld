package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ErrorResourceTest {

  @Test
  public void should_create_error_resource() {
    FieldErrorResource fieldError =
        new FieldErrorResource("user", "email", "invalid", "invalid email format");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    assertThat(errorResource.getFieldErrors().size(), is(1));
    assertThat(errorResource.getFieldErrors().get(0).getField(), is("email"));
  }

  @Test
  public void should_create_empty_error_resource() {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    assertThat(errorResource.getFieldErrors().size(), is(0));
  }

  @Test
  public void should_create_with_multiple_errors() {
    FieldErrorResource fieldError1 =
        new FieldErrorResource("user", "email", "invalid", "invalid email format");
    FieldErrorResource fieldError2 =
        new FieldErrorResource("user", "username", "taken", "already taken");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError1, fieldError2));

    assertThat(errorResource.getFieldErrors().size(), is(2));
  }
}
