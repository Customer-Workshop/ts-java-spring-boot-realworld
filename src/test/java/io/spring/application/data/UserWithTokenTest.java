package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserWithTokenTest {

  @Test
  void should_create_from_user_data_and_token() {
    UserData userData = new UserData("id1", "test@email.com", "user1", "bio text", "http://img.png");
    UserWithToken uwt = new UserWithToken(userData, "jwt-token-123");

    assertEquals("test@email.com", uwt.getEmail());
    assertEquals("user1", uwt.getUsername());
    assertEquals("bio text", uwt.getBio());
    assertEquals("http://img.png", uwt.getImage());
    assertEquals("jwt-token-123", uwt.getToken());
  }

  @Test
  void should_handle_null_bio_and_image() {
    UserData userData = new UserData("id1", "test@email.com", "user1", null, null);
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertNull(uwt.getBio());
    assertNull(uwt.getImage());
    assertEquals("token", uwt.getToken());
  }
}
