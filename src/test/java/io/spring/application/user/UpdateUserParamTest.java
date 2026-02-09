package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  public void should_create_update_user_param() {
    UpdateUserParam param = new UpdateUserParam("test@example.com", "password123", "testuser", "bio", "image.jpg");

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getPassword(), is("password123"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getBio(), is("bio"));
    assertThat(param.getImage(), is("image.jpg"));
  }

  @Test
  public void should_create_with_builder() {
    UpdateUserParam param = UpdateUserParam.builder()
        .email("test@example.com")
        .username("testuser")
        .bio("bio text")
        .build();

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getBio(), is("bio text"));
  }

  @Test
  public void should_have_default_empty_values() {
    UpdateUserParam param = new UpdateUserParam();

    assertThat(param.getEmail(), is(""));
    assertThat(param.getPassword(), is(""));
    assertThat(param.getUsername(), is(""));
    assertThat(param.getBio(), is(""));
    assertThat(param.getImage(), is(""));
  }
}
