package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProfileDataTest {

  @Test
  void should_create_with_all_args_constructor() {
    ProfileData data = new ProfileData("id1", "user1", "bio text", "http://img.png", true);

    assertEquals("id1", data.getId());
    assertEquals("user1", data.getUsername());
    assertEquals("bio text", data.getBio());
    assertEquals("http://img.png", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_with_no_arg_constructor_and_setters() {
    ProfileData data = new ProfileData();
    data.setId("id2");
    data.setUsername("user2");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(false);

    assertEquals("id2", data.getId());
    assertEquals("user2", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertFalse(data.isFollowing());
  }

  @Test
  void should_support_equals_and_hashCode() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio", "img", true);
    ProfileData data2 = new ProfileData("id1", "user1", "bio", "img", true);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio", "img", true);
    ProfileData data2 = new ProfileData("id2", "user2", "bio2", "img2", false);

    assertNotEquals(data1, data2);
  }

  @Test
  void should_support_toString() {
    ProfileData data = new ProfileData("id1", "user1", "bio", "img", true);
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("user1"));
  }
}
