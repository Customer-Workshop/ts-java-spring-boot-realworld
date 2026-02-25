package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserDataTest {

  @Test
  void should_create_with_all_args_constructor() {
    UserData data = new UserData("id1", "test@email.com", "user1", "bio text", "http://img.png");

    assertEquals("id1", data.getId());
    assertEquals("test@email.com", data.getEmail());
    assertEquals("user1", data.getUsername());
    assertEquals("bio text", data.getBio());
    assertEquals("http://img.png", data.getImage());
  }

  @Test
  void should_create_with_no_arg_constructor_and_setters() {
    UserData data = new UserData();
    data.setId("id2");
    data.setEmail("new@email.com");
    data.setUsername("user2");
    data.setBio("new bio");
    data.setImage("new img");

    assertEquals("id2", data.getId());
    assertEquals("new@email.com", data.getEmail());
    assertEquals("user2", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new img", data.getImage());
  }

  @Test
  void should_support_equals_and_hashCode() {
    UserData data1 = new UserData("id1", "a@b.com", "user1", "bio", "img");
    UserData data2 = new UserData("id1", "a@b.com", "user1", "bio", "img");

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    UserData data1 = new UserData("id1", "a@b.com", "user1", "bio", "img");
    UserData data2 = new UserData("id2", "c@d.com", "user2", "bio2", "img2");

    assertNotEquals(data1, data2);
  }

  @Test
  void should_support_toString() {
    UserData data = new UserData("id1", "a@b.com", "user1", "bio", "img");
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("user1"));
  }
}
