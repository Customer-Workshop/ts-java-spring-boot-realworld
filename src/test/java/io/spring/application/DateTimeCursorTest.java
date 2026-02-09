package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_date_time_cursor() {
    LocalDateTime now = LocalDateTime.now();
    DateTimeCursor cursor = new DateTimeCursor(now);

    assertThat(cursor.getData(), is(now));
  }

  @Test
  public void should_convert_to_string() {
    LocalDateTime dateTime = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    String result = cursor.toString();
    assertThat(result, notNullValue());
    assertThat(result.length() > 0, is(true));
  }

  @Test
  public void should_return_empty_string_for_null_data() {
    DateTimeCursor cursor = new DateTimeCursor(null);

    assertThat(cursor.toString(), is(""));
  }

  @Test
  public void should_parse_cursor_string() {
    LocalDateTime original = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    long millis = original.toInstant(ZoneOffset.UTC).toEpochMilli();

    LocalDateTime parsed = DateTimeCursor.parse(String.valueOf(millis));

    assertThat(parsed, notNullValue());
    assertThat(parsed.getYear(), is(2026));
    assertThat(parsed.getMonthValue(), is(1));
    assertThat(parsed.getDayOfMonth(), is(15));
  }

  @Test
  public void should_return_null_for_null_cursor() {
    LocalDateTime result = DateTimeCursor.parse(null);

    assertThat(result, nullValue());
  }

  @Test
  public void should_return_null_for_empty_cursor() {
    LocalDateTime result = DateTimeCursor.parse("");

    assertThat(result, nullValue());
  }
}
