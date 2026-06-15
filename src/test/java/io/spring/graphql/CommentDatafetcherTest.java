package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentDatafetcherTest {

  private CommentQueryService commentQueryService;
  private CommentDatafetcher commentDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    commentQueryService = mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_payload() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CommentData commentData =
        new CommentData(
            "comment1",
            "body text",
            "articleId",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "author", "bio", "img", false));
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("comment1", result.getData().getId());
    assertEquals("body text", result.getData().getBody());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
  }

  @Test
  void should_get_article_comments_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article article = Article.newBuilder().slug("test-article").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData =
        new ArticleData(
            "artId",
            "test-article",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList(),
            new ProfileData("uid", "author", "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData =
        new CommentData(
            "c1",
            "comment body",
            "artId",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "commenter", "", "", false));
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("c1", result.getData().getEdges().get(0).getNode().getId());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void should_get_article_comments_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article article = Article.newBuilder().slug("test-article").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData =
        new ArticleData(
            "artId",
            "test-article",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList(),
            new ProfileData("uid", "author", "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData =
        new CommentData(
            "c1",
            "comment body",
            "artId",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "commenter", "", "", false));
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void should_throw_when_first_and_last_both_null() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_handle_empty_comments() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article article = Article.newBuilder().slug("test-article").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData =
        new ArticleData(
            "artId",
            "test-article",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList(),
            new ProfileData("uid", "author", "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
