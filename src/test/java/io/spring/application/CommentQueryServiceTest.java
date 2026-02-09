package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
  }

  @Test
  public void should_find_comment_by_id() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData commentData =
        new CommentData("comment-1", "Test comment", "article-1", now, now, profile);
    when(commentReadService.findById("comment-1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    User user = new User("test@example.com", "testuser", "password", "", "");
    Optional<CommentData> result = commentQueryService.findById("comment-1", user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getBody(), is("Test comment"));
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);

    User user = new User("test@example.com", "testuser", "password", "", "");
    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_find_comments_by_article_id() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment1 =
        new CommentData("comment-1", "Comment 1", "article-1", now, now, profile);
    CommentData comment2 =
        new CommentData("comment-2", "Comment 2", "article-1", now, now, profile);
    when(commentReadService.findByArticleId("article-1"))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    User user = new User("test@example.com", "testuser", "password", "", "");
    List<CommentData> result = commentQueryService.findByArticleId("article-1", user);

    assertThat(result.size(), is(2));
  }

  @Test
  public void should_return_empty_list_when_no_comments() {
    when(commentReadService.findByArticleId("article-1")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-1", null);

    assertThat(result.size(), is(0));
  }

  @Test
  public void should_find_comments_with_cursor() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment = new CommentData("comment-1", "Comment 1", "article-1", now, now, profile);
    CursorPageParameter<LocalDateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(anyString(), any()))
        .thenReturn(Arrays.asList(comment));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", null, page);

    assertThat(result.getData().size(), is(1));
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<LocalDateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(anyString(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", null, page);

    assertThat(result.getData().size(), is(0));
  }
}
