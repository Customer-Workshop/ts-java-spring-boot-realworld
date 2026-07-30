package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeHandlerTest {
  @Test
  public void timestampConversionRoundTripsInUtc() throws Exception {
    Instant instant = Instant.parse("2026-07-29T20:57:31.123Z");
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    ResultSet resultSet = mock(ResultSet.class);
    Timestamp timestamp = new Timestamp(instant.toEpochMilli());
    DateTimeHandler handler = new DateTimeHandler();

    handler.setParameter(preparedStatement, 1, instant, null);
    verify(preparedStatement)
        .setTimestamp(
            org.mockito.ArgumentMatchers.eq(1),
            org.mockito.ArgumentMatchers.eq(timestamp),
            org.mockito.ArgumentMatchers.argThat(
                calendar -> calendar.getTimeZone().equals(TimeZone.getTimeZone("UTC"))));
    when(
            resultSet.getTimestamp(
                org.mockito.ArgumentMatchers.eq("created_at"),
                org.mockito.ArgumentMatchers.any(Calendar.class)))
        .thenReturn(timestamp);

    assertEquals(instant, handler.getResult(resultSet, "created_at"));
  }
}
