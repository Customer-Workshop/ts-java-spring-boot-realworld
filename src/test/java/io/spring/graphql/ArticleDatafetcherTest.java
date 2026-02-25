package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;

  private ArticleDatafetcher articleDatafetcher;
  private User currentUser;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    currentUser = new User("test@email.com", "testuser", "pass", "bio", "img");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData createArticleData(String slug, String title) {
    ArticleData data = new ArticleData();
    data.setSlug(slug);
    data.setTitle(title);
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(false);
    data.setFavoritesCount(0);
    data.setCreatedAt(new DateTime());
    data.setUpdatedAt(new DateTime());
    data.setTagList(Arrays.asList("tag1"));
    data.setProfileData(new ProfileData("uid", "author", "bio", "img", false));
    return data;
  }

  @Test
  void should_get_feed_with_first_parameter() {
    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("slug1", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void should_get_feed_with_last_parameter() {
    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_feed_with_empty_results() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void should_get_articles_with_first_parameter() {
    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, true);

    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "tag", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().getHasNextPage());
  }

  @Test
  void should_get_articles_with_last_parameter() {
    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.PREV, false);

    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("test-slug", "Test Title");
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-slug", result.getData().getSlug());
    assertEquals("Test Title", result.getData().getTitle());
  }

  @Test
  void should_throw_when_article_not_found_by_slug() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("missing"));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Title", "desc", "body", Arrays.asList("tag"), currentUser.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    ArticleData articleData = createArticleData("title", "Title");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertEquals("title", result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    CommentData commentData = new CommentData();
    commentData.setId("c1");
    commentData.setArticleId("a1");
    when(dfe.getLocalContext()).thenReturn(commentData);

    ArticleData articleData = createArticleData("slug", "Title");
    when(articleQueryService.findById(eq("a1"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertEquals("slug", result.getData().getSlug());
  }

  @Test
  void should_get_user_feed_from_profile() {
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);

    User target = new User("target@email.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);

    User target = new User("target@email.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites() {
    Profile profile = Profile.newBuilder().username("favuser").build();
    when(dfe.getSource()).thenReturn(profile);

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    Profile profile = Profile.newBuilder().username("favuser").build();
    when(dfe.getSource()).thenReturn(profile);

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_favorites_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles() {
    Profile profile = Profile.newBuilder().username("author1").build();
    when(dfe.getSource()).thenReturn(profile);

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    Profile profile = Profile.newBuilder().username("author1").build();
    when(dfe.getSource()).thenReturn(profile);

    List<ArticleData> articles = Arrays.asList(createArticleData("slug1", "Title 1"));
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_build_page_info_with_cursors() {
    ArticleData data = createArticleData("slug1", "Title 1");
    List<ArticleData> articles = Arrays.asList(data);
    CursorPager<ArticleData> pager = new CursorPager<>(articles, Direction.NEXT, true);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().getHasNextPage());
  }

  @Test
  void should_find_article_by_slug_when_anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anon",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    ArticleData articleData = createArticleData("test-slug", "Test Title");
    when(articleQueryService.findBySlug(eq("test-slug"), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }
}
