package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PageTest {

  @Test
  void should_create_default_page() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_create_page_with_valid_values() {
    Page page = new Page(5, 30);
    assertEquals(5, page.getOffset());
    assertEquals(30, page.getLimit());
  }

  @Test
  void should_cap_limit_at_max() {
    Page page = new Page(0, 200);
    assertEquals(0, page.getOffset());
    assertEquals(100, page.getLimit());
  }

  @Test
  void should_use_default_for_negative_offset() {
    Page page = new Page(-5, 10);
    assertEquals(0, page.getOffset());
    assertEquals(10, page.getLimit());
  }

  @Test
  void should_use_default_for_negative_limit() {
    Page page = new Page(0, -5);
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_use_default_for_zero_limit() {
    Page page = new Page(0, 0);
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void should_support_equals_and_hashcode() {
    Page page1 = new Page(5, 30);
    Page page2 = new Page(5, 30);
    assertEquals(page1, page2);
    assertEquals(page1.hashCode(), page2.hashCode());
  }

  @Test
  void should_support_to_string() {
    Page page = new Page(5, 30);
    assertNotNull(page.toString());
    assertTrue(page.toString().contains("5"));
    assertTrue(page.toString().contains("30"));
  }
}
