package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class ArticleDataTest {

  @Test
  void should_create_with_no_arg_constructor_and_setters() {
    ArticleData data = new ArticleData();
    data.setId("id1");
    data.setSlug("test-slug");
    data.setTitle("Test Title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(5);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    List<String> tags = Arrays.asList("java", "spring");
    data.setTagList(tags);
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("id1", data.getId());
    assertEquals("test-slug", data.getSlug());
    assertEquals("Test Title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(tags, data.getTagList());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_all_args_constructor() {
    DateTime now = new DateTime();
    List<String> tags = Arrays.asList("java");
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    ArticleData data =
        new ArticleData("id1", "slug", "title", "desc", "body", true, 3, now, now, tags, profile);

    assertEquals("id1", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(3, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(tags, data.getTagList());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_return_cursor_from_updatedAt() {
    DateTime now = new DateTime();
    ArticleData data = new ArticleData();
    data.setUpdatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_support_equals_and_hashCode() {
    DateTime now = new DateTime();
    List<String> tags = Arrays.asList("java");
    ProfileData profile = new ProfileData("pid", "user1", "bio", "img", false);
    ArticleData data1 =
        new ArticleData("id1", "slug", "title", "desc", "body", true, 3, now, now, tags, profile);
    ArticleData data2 =
        new ArticleData("id1", "slug", "title", "desc", "body", true, 3, now, now, tags, profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_support_toString() {
    ArticleData data = new ArticleData();
    data.setId("id1");
    data.setTitle("Test");
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("id1"));
  }
}
