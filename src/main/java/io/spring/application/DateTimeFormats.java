package io.spring.application;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeFormats {
  public static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private DateTimeFormats() {}
}
