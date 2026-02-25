package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PageCursorTest {

  @Test
  void should_return_data() {
    DateTimeCursor cursor = new DateTimeCursor(new org.joda.time.DateTime());
    assertNotNull(cursor.getData());
  }

  @Test
  void should_convert_to_string() {
    org.joda.time.DateTime now = new org.joda.time.DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);
    String str = cursor.toString();
    assertNotNull(str);
    assertEquals(String.valueOf(now.getMillis()), str);
  }
}
