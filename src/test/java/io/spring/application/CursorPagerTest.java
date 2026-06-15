package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData buildArticle(String slug) {
    return new ArticleData(
        "id",
        slug,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        null,
        new ProfileData("uid", "user", "", "", false));
  }

  @Test
  void should_have_next_when_direction_is_next_and_has_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(buildArticle("s1")), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(1, pager.getData().size());
  }

  @Test
  void should_not_have_next_when_direction_is_next_and_no_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(buildArticle("s1")), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_is_prev_and_has_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(buildArticle("s1")), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_have_previous_when_direction_is_prev_and_no_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(buildArticle("s1")), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_correct_start_and_end_cursors() {
    ArticleData a1 = buildArticle("s1");
    ArticleData a2 = buildArticle("s2");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(a1.getCursor().toString(), pager.getStartCursor().toString());
    assertEquals(a2.getCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  void should_create_cursor_page_parameter() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(11, param.getQueryLimit());
    assertEquals(Direction.NEXT, param.getDirection());
    assertTrue(param.isNext());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_use_default_limit_for_zero_or_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.PREV);
    assertEquals(20, param.getLimit());
    assertFalse(param.isNext());
  }

  @Test
  void should_create_default_cursor_page_parameter() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }

  @Test
  void should_parse_date_time_cursor() {
    DateTime now = new DateTime();
    DateTimeCursor cursor = new DateTimeCursor(now);

    String str = cursor.toString();
    assertNotNull(str);

    DateTime parsed = DateTimeCursor.parse(str);
    assertNotNull(parsed);
    assertEquals(now.getMillis(), parsed.getMillis());
  }

  @Test
  void should_return_null_for_null_cursor_parse() {
    assertNull(DateTimeCursor.parse(null));
  }
}
