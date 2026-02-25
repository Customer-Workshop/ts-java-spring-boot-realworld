package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;

  @BeforeEach
  void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
  }

  private CommentData createCommentData(String id, String profileId) {
    CommentData data = new CommentData();
    data.setId(id);
    data.setBody("body");
    data.setArticleId("a1");
    data.setCreatedAt(new DateTime());
    data.setProfileData(new ProfileData(profileId, "author", "bio", "img", false));
    return data;
  }

  @Test
  void should_find_comment_by_id() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    CommentData commentData = createCommentData("c1", "p1");
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "p1")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    when(commentReadService.findById("missing")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_comments_by_article_id_with_user() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    CommentData c1 = createCommentData("c1", "p1");
    CommentData c2 = createCommentData("c2", "p2");
    when(commentReadService.findByArticleId("a1")).thenReturn(Arrays.asList(c1, c2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("p1")));

    List<CommentData> results = commentQueryService.findByArticleId("a1", user);

    assertEquals(2, results.size());
    assertTrue(results.get(0).getProfileData().isFollowing());
    assertFalse(results.get(1).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    CommentData c1 = createCommentData("c1", "p1");
    when(commentReadService.findByArticleId("a1")).thenReturn(Arrays.asList(c1));

    List<CommentData> results = commentQueryService.findByArticleId("a1", null);

    assertEquals(1, results.size());
    assertFalse(results.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    CommentData c1 = createCommentData("c1", "p1");
    CommentData c2 = createCommentData("c2", "p2");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Arrays.asList(c1, c2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("p1")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
  }

  @Test
  void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_handle_extra_results_for_cursor_pagination() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    CommentData c1 = createCommentData("c1", "p1");
    CommentData c2 = createCommentData("c2", "p2");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new java.util.ArrayList<>(Arrays.asList(c1, c2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_reverse_results_for_prev_direction() {
    CommentData c1 = createCommentData("c1", "p1");
    CommentData c2 = createCommentData("c2", "p2");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Arrays.asList(c1, c2));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertEquals("c2", result.getData().get(0).getId());
    assertEquals("c1", result.getData().get(1).getId());
  }
}
