package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArticleFavoriteCountTest {

  @Test
  void should_create_with_id_and_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("a1", 5);

    assertEquals("a1", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  void should_be_immutable_value_object() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("a1", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("a1", 5);

    assertEquals(count1, count2);
    assertEquals(count1.hashCode(), count2.hashCode());
  }

  @Test
  void should_not_equal_different_values() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("a1", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("a2", 10);

    assertNotEquals(count1, count2);
  }

  @Test
  void should_support_toString() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("a1", 5);
    assertNotNull(count.toString());
    assertTrue(count.toString().contains("a1"));
  }
}
