package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {
  @Test
  public void toStringUsesEpochMillisAndParseRoundTrips() {
    Instant instant = Instant.parse("2026-07-29T20:57:31.123Z");
    DateTimeCursor cursor = new DateTimeCursor(instant);

    assertEquals(String.valueOf(instant.toEpochMilli()), cursor.toString());
    assertEquals(instant, DateTimeCursor.parse(cursor.toString()));
  }
}
