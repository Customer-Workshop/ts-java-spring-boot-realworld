package io.spring.application;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ProfileData profileData = new ProfileData("id", "user", "bio", "image", false);

  private CommentData createComment(String id, DateTime createdAt) {
    return new CommentData(id, "body", "articleId", createdAt, createdAt, profileData);
  }

  @Test
  public void should_set_next_true_when_has_extra_in_next_direction() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);
    CommentData comment2 = createComment("c2", now.plusMinutes(1));

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1, comment2), Direction.NEXT, true);

    Assertions.assertTrue(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_set_next_false_when_no_extra_in_next_direction() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1), Direction.NEXT, false);

    Assertions.assertFalse(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_set_previous_true_when_has_extra_in_prev_direction() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1), Direction.PREV, true);

    Assertions.assertFalse(pager.hasNext());
    Assertions.assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_set_previous_false_when_no_extra_in_prev_direction() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1), Direction.PREV, false);

    Assertions.assertFalse(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_return_null_start_cursor_when_data_empty() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    Assertions.assertNull(pager.getStartCursor());
  }

  @Test
  public void should_return_null_end_cursor_when_data_empty() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    Assertions.assertNull(pager.getEndCursor());
  }

  @Test
  public void should_return_start_cursor_from_first_element() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);
    CommentData comment2 = createComment("c2", now.plusMinutes(1));

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1, comment2), Direction.NEXT, false);

    Assertions.assertNotNull(pager.getStartCursor());
    Assertions.assertEquals(String.valueOf(now.getMillis()), pager.getStartCursor().toString());
  }

  @Test
  public void should_return_end_cursor_from_last_element() {
    DateTime now = new DateTime();
    DateTime later = now.plusMinutes(5);
    CommentData comment1 = createComment("c1", now);
    CommentData comment2 = createComment("c2", later);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1, comment2), Direction.NEXT, false);

    Assertions.assertNotNull(pager.getEndCursor());
    Assertions.assertEquals(String.valueOf(later.getMillis()), pager.getEndCursor().toString());
  }

  @Test
  public void should_return_same_cursor_for_single_element() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1), Direction.NEXT, false);

    Assertions.assertEquals(pager.getStartCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  public void should_get_data_list() {
    DateTime now = new DateTime();
    CommentData comment1 = createComment("c1", now);
    CommentData comment2 = createComment("c2", now.plusMinutes(1));

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment1, comment2), Direction.NEXT, false);

    Assertions.assertEquals(2, pager.getData().size());
    Assertions.assertEquals("c1", pager.getData().get(0).getId());
    Assertions.assertEquals("c2", pager.getData().get(1).getId());
  }

  @Test
  public void should_handle_empty_list_with_prev_direction() {
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);

    Assertions.assertTrue(pager.getData().isEmpty());
    Assertions.assertFalse(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
    Assertions.assertNull(pager.getStartCursor());
    Assertions.assertNull(pager.getEndCursor());
  }
}
