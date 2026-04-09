package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentQueryServiceCursorTest {

  @Mock private CommentReadService commentReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    Assertions.assertTrue(result.getData().isEmpty());
    Assertions.assertFalse(result.hasNext());
  }

  @Test
  public void should_return_comments_with_cursor_for_next_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    DateTime now = new DateTime();

    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);

    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(comment1)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    Assertions.assertEquals(1, result.getData().size());
    Assertions.assertFalse(result.hasNext());
  }

  @Test
  public void should_set_has_next_when_extra_results() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    DateTime now = new DateTime();

    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);
    CommentData comment2 =
        new CommentData(
            "c2", "body2", "article1", now.plusMinutes(1), now.plusMinutes(1), profileData);

    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(comment1, comment2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    Assertions.assertEquals(1, result.getData().size());
    Assertions.assertTrue(result.hasNext());
  }

  @Test
  public void should_reverse_comments_for_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.PREV);
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    DateTime now = new DateTime();

    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);
    CommentData comment2 =
        new CommentData(
            "c2", "body2", "article1", now.plusMinutes(1), now.plusMinutes(1), profileData);

    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(comment1, comment2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    Assertions.assertEquals(2, result.getData().size());
    Assertions.assertEquals("c2", result.getData().get(0).getId());
    Assertions.assertEquals("c1", result.getData().get(1).getId());
  }

  @Test
  public void should_set_following_for_followed_authors_with_cursor() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    ProfileData profileData = new ProfileData("author1", "authoruser", "", "", false);
    DateTime now = new DateTime();

    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);

    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(comment1)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    Assertions.assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_not_query_following_when_user_is_null_with_cursor() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 20, Direction.NEXT);
    ProfileData profileData = new ProfileData("author1", "authoruser", "", "", false);
    DateTime now = new DateTime();

    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);

    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(comment1)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", null, page);

    Assertions.assertFalse(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comment_by_id_returns_empty_when_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    Assertions.assertFalse(result.isPresent());
  }

  @Test
  public void should_find_by_article_id_returns_empty_when_no_comments() {
    when(commentReadService.findByArticleId("article1")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article1", user);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_by_article_id_sets_following_for_followed_authors() {
    ProfileData profileData = new ProfileData("author1", "authoruser", "", "", false);
    DateTime now = new DateTime();
    CommentData comment1 = new CommentData("c1", "body1", "article1", now, now, profileData);

    when(commentReadService.findByArticleId("article1")).thenReturn(Arrays.asList(comment1));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author1")));

    List<CommentData> result = commentQueryService.findByArticleId("article1", user);

    Assertions.assertTrue(result.get(0).getProfileData().isFollowing());
  }
}
