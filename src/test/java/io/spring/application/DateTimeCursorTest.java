package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

class DateTimeCursorTest {

  @Test
  void should_create_from_datetime() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertEquals(now, cursor.getData());
  }

  @Test
  void should_convert_to_string_as_millis() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertEquals(String.valueOf(now.getMillis()), cursor.toString());
  }

  @Test
  void should_parse_null_to_null() {
    assertNull(DateTimeCursor.parse(null));
  }

  @Test
  void should_parse_millis_string_to_datetime() {
    DateTime original = new DateTime().withZone(DateTimeZone.UTC);
    String millis = String.valueOf(original.getMillis());
    DateTime parsed = DateTimeCursor.parse(millis);

    assertNotNull(parsed);
    assertEquals(original.getMillis(), parsed.getMillis());
    assertEquals(DateTimeZone.UTC, parsed.getZone());
  }
}
