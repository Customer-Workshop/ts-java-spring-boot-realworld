package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void should_create_user_with_constructor() {
    User user = new User("test@email.com", "testuser", "password123", "bio text", "http://img.png");

    assertNotNull(user.getId());
    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password123", user.getPassword());
    assertEquals("bio text", user.getBio());
    assertEquals("http://img.png", user.getImage());
  }

  @Test
  void should_generate_unique_ids() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  void should_update_all_fields() {
    User user = new User("old@email.com", "olduser", "oldpass", "old bio", "old img");
    user.update("new@email.com", "newuser", "newpass", "new bio", "new img");

    assertEquals("new@email.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new img", user.getImage());
  }

  @Test
  void should_not_update_empty_fields() {
    User user = new User("test@email.com", "testuser", "password", "bio", "img");
    user.update("", "", "", "", "");

    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void should_not_update_null_fields() {
    User user = new User("test@email.com", "testuser", "password", "bio", "img");
    user.update(null, null, null, null, null);

    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void should_partially_update_fields() {
    User user = new User("test@email.com", "testuser", "password", "bio", "img");
    user.update("new@email.com", "", null, "new bio", "");

    assertEquals("new@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void should_support_equals_based_on_id() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");

    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }

  @Test
  void should_create_with_no_arg_constructor() {
    User user = new User();
    assertNull(user.getId());
    assertNull(user.getEmail());
  }
}
