package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleDatafetcherTest {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    articleQueryService = mock(ArticleQueryService.class);
    userRepository = mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData buildArticleData(String slug) {
    return new ArticleData(
        "id1",
        slug,
        "Title",
        "description",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Arrays.asList("java"),
        new ProfileData("authorId", "author", "bio", "img", false));
  }

  @Test
  void should_get_feed_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = buildArticleData("test-slug");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("test-slug", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void should_get_feed_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = buildArticleData("test-slug");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_first_and_last_both_null_in_feed() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_feed() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = buildArticleData("user-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void should_get_user_feed_with_last_param() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = buildArticleData("user-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void should_throw_when_first_and_last_both_null_in_user_feed() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = buildArticleData("fav-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = buildArticleData("fav-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_first_and_last_both_null_in_user_favorites() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("author").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = buildArticleData("my-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), eq("author"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("author").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = buildArticleData("my-article");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), eq("author"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_first_and_last_both_null_in_user_articles() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = buildArticleData("article-1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "java", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = buildArticleData("article-1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_first_and_last_both_null_in_get_articles() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_article_from_payload() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    ArticleData articleData = buildArticleData("title");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertEquals("title", result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    CommentData commentData =
        new CommentData("cid", "body", "articleId", new DateTime(), new DateTime(), null);
    when(dfe.getLocalContext()).thenReturn(commentData);

    ArticleData articleData = buildArticleData("slug");
    when(articleQueryService.findById(eq("articleId"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertEquals("slug", result.getData().getSlug());
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = buildArticleData("my-slug");
    when(articleQueryService.findBySlug(eq("my-slug"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("my-slug");

    assertNotNull(result);
    assertEquals("my-slug", result.getData().getSlug());
    assertEquals("Title", result.getData().getTitle());
    assertEquals("description", result.getData().getDescription());
    assertEquals("body", result.getData().getBody());
    assertFalse(result.getData().getFavorited());
    assertEquals(0, result.getData().getFavoritesCount());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
    assertEquals(Arrays.asList("java"), result.getData().getTagList());
  }

  @Test
  void should_handle_empty_pager_results() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CursorPager<ArticleData> emptyPager = new CursorPager<>(Arrays.asList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(emptyPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
    assertNull(result.getData().getPageInfo().getStartCursor());
    assertNull(result.getData().getPageInfo().getEndCursor());
  }
}
