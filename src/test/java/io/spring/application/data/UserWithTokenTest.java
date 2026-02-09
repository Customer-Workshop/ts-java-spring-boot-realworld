package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  public void should_create_user_with_token() {
    UserData userData =
        new UserData("user-1", "test@example.com", "testuser", "bio text", "image.jpg");
    UserWithToken userWithToken = new UserWithToken(userData, "jwt-token-123");

    assertThat(userWithToken.getEmail(), is("test@example.com"));
    assertThat(userWithToken.getUsername(), is("testuser"));
    assertThat(userWithToken.getBio(), is("bio text"));
    assertThat(userWithToken.getImage(), is("image.jpg"));
    assertThat(userWithToken.getToken(), is("jwt-token-123"));
  }

  @Test
  public void should_handle_null_bio_and_image() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", null, null);
    UserWithToken userWithToken = new UserWithToken(userData, "jwt-token-123");

    assertThat(userWithToken.getEmail(), is("test@example.com"));
    assertThat(userWithToken.getUsername(), is("testuser"));
    assertThat(userWithToken.getBio() == null, is(true));
    assertThat(userWithToken.getImage() == null, is(true));
  }
}
