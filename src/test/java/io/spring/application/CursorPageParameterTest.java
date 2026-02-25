package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPageParameterTest {

  @Test
  void should_create_with_constructor() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), 5000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
    assertEquals(1001, param.getQueryLimit());
  }

  @Test
  void should_not_change_limit_for_zero_or_negative() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_not_change_limit_for_negative() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_return_is_next_true_for_next_direction() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void should_return_is_next_false_for_prev_direction() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(new DateTime(), 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void should_create_with_no_arg_constructor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertNull(param.getDirection());
  }
}
