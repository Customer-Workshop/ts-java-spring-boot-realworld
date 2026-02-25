package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CommentDataTest {

  @Test
  void should_create_with_no_arg_constructor_and_setters() {
    CommentData data = new CommentData();
    data.setId("c1");
    data.setBody("comment body");
    data.setArticleId("a1");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("c1", data.getId());
    assertEquals("comment body", data.getBody());
    assertEquals("a1", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_all_args_constructor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    CommentData data = new CommentData("c1", "body", "a1", now, now, profile);

    assertEquals("c1", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("a1", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_return_cursor_from_createdAt() {
    DateTime now = new DateTime();
    CommentData data = new CommentData();
    data.setCreatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_support_equals_and_hashCode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    CommentData data1 = new CommentData("c1", "body", "a1", now, now, profile);
    CommentData data2 = new CommentData("c1", "body", "a1", now, now, profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_support_toString() {
    CommentData data = new CommentData();
    data.setId("c1");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("c1"));
  }
}
