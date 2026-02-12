package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_convert_to_string_as_millis() {
    DateTime time = new DateTime(2025, 1, 1, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(time);
    assertThat(cursor.toString(), is(String.valueOf(time.getMillis())));
  }

  @Test
  public void should_parse_millis_string_to_datetime() {
    DateTime time = new DateTime(2025, 6, 15, 12, 30, DateTimeZone.UTC);
    String millis = String.valueOf(time.getMillis());
    DateTime parsed = DateTimeCursor.parse(millis);
    assertThat(parsed.getMillis(), is(time.getMillis()));
    assertThat(parsed.getZone(), is(DateTimeZone.UTC));
  }

  @Test
  public void should_return_null_for_null_cursor() {
    assertThat(DateTimeCursor.parse(null), nullValue());
  }

  @Test
  public void should_round_trip_correctly() {
    DateTime original = new DateTime(2025, 3, 20, 8, 45, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(original);
    DateTime parsed = DateTimeCursor.parse(cursor.toString());
    assertThat(parsed.getMillis(), is(original.getMillis()));
  }
}
