package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPagerTest {

  @Test
  void should_set_next_true_when_direction_next_and_has_extra() {
    ArticleData article = createArticleData();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertTrue(pager.isNext());
    assertFalse(pager.isPrevious());
  }

  @Test
  void should_set_next_false_when_direction_next_and_no_extra() {
    ArticleData article = createArticleData();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_set_previous_true_when_direction_prev_and_has_extra() {
    ArticleData article = createArticleData();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_set_previous_false_when_direction_prev_and_no_extra() {
    ArticleData article = createArticleData();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);

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
  void should_return_cursors_for_non_empty_data() {
    DateTime now = new DateTime();
    ArticleData article1 = createArticleData();
    article1.setUpdatedAt(now);
    ArticleData article2 = createArticleData();
    article2.setUpdatedAt(now.plusHours(1));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article1, article2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(now, pager.getStartCursor().getData());
    assertEquals(now.plusHours(1), pager.getEndCursor().getData());
  }

  @Test
  void should_return_data_list() {
    ArticleData article = createArticleData();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);

    assertEquals(1, pager.getData().size());
  }

  private ArticleData createArticleData() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setUpdatedAt(new DateTime());
    return data;
  }
}
