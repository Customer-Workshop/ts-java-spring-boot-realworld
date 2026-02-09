package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_create_cursor_pager_with_next_direction() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article = new ArticleData(
        "article-1", "test-slug", "Test Title", "Test Description", "Test Body",
        false, 5, now, now, Arrays.asList("java"), profile);

    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), CursorPager.Direction.NEXT, true);

    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(true));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_create_cursor_pager_with_prev_direction() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article = new ArticleData(
        "article-1", "test-slug", "Test Title", "Test Description", "Test Body",
        false, 5, now, now, Arrays.asList("java"), profile);

    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), CursorPager.Direction.PREV, true);

    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(true));
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager = new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), nullValue());
    assertThat(pager.getEndCursor(), nullValue());
  }

  @Test
  public void should_return_cursors_for_non_empty_data() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article = new ArticleData(
        "article-1", "test-slug", "Test Title", "Test Description", "Test Body",
        false, 5, now, now, Arrays.asList("java"), profile);

    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), notNullValue());
    assertThat(pager.getEndCursor(), notNullValue());
  }

  @Test
  public void should_not_have_extra_when_false() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData article = new ArticleData(
        "article-1", "test-slug", "Test Title", "Test Description", "Test Body",
        false, 5, now, now, Arrays.asList("java"), profile);

    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), CursorPager.Direction.NEXT, false);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }
}
