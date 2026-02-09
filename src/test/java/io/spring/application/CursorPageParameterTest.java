package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_cursor_page_parameter_with_next_direction() {
    LocalDateTime cursor = LocalDateTime.now();
    CursorPageParameter<LocalDateTime> param =
        new CursorPageParameter<>(cursor, 10, CursorPager.Direction.NEXT);

    assertThat(param.getCursor(), is(cursor));
    assertThat(param.getLimit(), is(10));
    assertThat(param.getQueryLimit(), is(11));
    assertThat(param.getDirection(), is(CursorPager.Direction.NEXT));
    assertThat(param.isNext(), is(true));
  }

  @Test
  public void should_create_cursor_page_parameter_with_prev_direction() {
    LocalDateTime cursor = LocalDateTime.now();
    CursorPageParameter<LocalDateTime> param =
        new CursorPageParameter<>(cursor, 10, CursorPager.Direction.PREV);

    assertThat(param.getCursor(), is(cursor));
    assertThat(param.getLimit(), is(10));
    assertThat(param.getQueryLimit(), is(11));
    assertThat(param.getDirection(), is(CursorPager.Direction.PREV));
    assertThat(param.isNext(), is(false));
  }

  @Test
  public void should_handle_null_cursor() {
    CursorPageParameter<LocalDateTime> param =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    assertThat(param.getCursor(), nullValue());
    assertThat(param.getLimit(), is(10));
    assertThat(param.getQueryLimit(), is(11));
  }

  @Test
  public void should_cap_limit_at_max() {
    CursorPageParameter<LocalDateTime> param =
        new CursorPageParameter<>(null, 2000, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_use_default_for_negative_limit() {
    CursorPageParameter<LocalDateTime> param =
        new CursorPageParameter<>(null, -5, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(20));
  }
}
