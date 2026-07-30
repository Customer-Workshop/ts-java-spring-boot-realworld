package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations.RealWorldModules;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class DateTimeFormatsTest {
  private final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new RealWorldModules());

  @Test
  public void jacksonSerializerUsesThreeDigitUtcMillisForWholeSecond() throws Exception {
    Instant instant = Instant.parse("2026-07-29T20:57:31Z");

    assertEquals("\"2026-07-29T20:57:31.000Z\"", objectMapper.writeValueAsString(instant));
    assertEquals("2026-07-29T20:57:31.000Z", DateTimeFormats.DATE_TIME_FORMAT.format(instant));
  }

  @Test
  public void jacksonSerializerUsesMillisForNonZeroMillis() throws Exception {
    Instant instant = Instant.parse("2026-07-29T20:57:31.123Z");

    assertEquals("\"2026-07-29T20:57:31.123Z\"", objectMapper.writeValueAsString(instant));
    assertEquals("2026-07-29T20:57:31.123Z", DateTimeFormats.DATE_TIME_FORMAT.format(instant));
  }
}
