package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_have_next_when_direction_is_next_and_has_extra() {
    CursorPager<ArticleDataNode> pager =
        new CursorPager<>(Arrays.asList(createNode()), CursorPager.Direction.NEXT, true);
    assertThat(pager.hasNext(), is(true));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_not_have_next_when_direction_is_next_and_no_extra() {
    CursorPager<ArticleDataNode> pager =
        new CursorPager<>(Arrays.asList(createNode()), CursorPager.Direction.NEXT, false);
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_have_previous_when_direction_is_prev_and_has_extra() {
    CursorPager<ArticleDataNode> pager =
        new CursorPager<>(Arrays.asList(createNode()), CursorPager.Direction.PREV, true);
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(true));
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleDataNode> pager =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    assertThat(pager.getStartCursor(), nullValue());
    assertThat(pager.getEndCursor(), nullValue());
  }

  @Test
  public void should_return_start_and_end_cursors_for_data() {
    DateTime time1 = new DateTime(2025, 1, 1, 0, 0);
    DateTime time2 = new DateTime(2025, 6, 1, 0, 0);
    ArticleDataNode node1 = new ArticleDataNode(time1);
    ArticleDataNode node2 = new ArticleDataNode(time2);
    CursorPager<ArticleDataNode> pager =
        new CursorPager<>(Arrays.asList(node1, node2), CursorPager.Direction.NEXT, false);
    assertThat(pager.getStartCursor().getData(), is(time1));
    assertThat(pager.getEndCursor().getData(), is(time2));
  }

  private ArticleDataNode createNode() {
    return new ArticleDataNode(new DateTime());
  }

  private static class ArticleDataNode implements Node {
    private final DateTime createdAt;

    ArticleDataNode(DateTime createdAt) {
      this.createdAt = createdAt;
    }

    @Override
    public PageCursor getCursor() {
      return new DateTimeCursor(createdAt);
    }
  }
}
