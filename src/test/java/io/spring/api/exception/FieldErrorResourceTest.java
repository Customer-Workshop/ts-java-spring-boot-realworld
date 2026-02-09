package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FieldErrorResourceTest {

  @Test
  public void should_create_field_error_resource() {
    FieldErrorResource fieldError =
        new FieldErrorResource("user", "email", "invalid", "invalid email format");

    assertThat(fieldError.getResource(), is("user"));
    assertThat(fieldError.getField(), is("email"));
    assertThat(fieldError.getCode(), is("invalid"));
    assertThat(fieldError.getMessage(), is("invalid email format"));
  }

  @Test
  public void should_handle_null_values() {
    FieldErrorResource fieldError = new FieldErrorResource(null, null, null, null);

    assertThat(fieldError.getResource() == null, is(true));
    assertThat(fieldError.getField() == null, is(true));
    assertThat(fieldError.getCode() == null, is(true));
    assertThat(fieldError.getMessage() == null, is(true));
  }
}
