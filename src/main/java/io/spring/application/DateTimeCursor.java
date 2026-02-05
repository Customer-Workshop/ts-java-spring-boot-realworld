package io.spring.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeCursor extends PageCursor<LocalDateTime> {

  public DateTimeCursor(LocalDateTime data) {
    super(data);
  }

  @Override
  public String toString() {
    if (getData() == null) {
      return "";
    }
    return String.valueOf(getData().toInstant(ZoneOffset.UTC).toEpochMilli());
  }

  public static LocalDateTime parse(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return null;
    }
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(cursor)), ZoneOffset.UTC);
  }
}
