package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArticleQueryServiceTest {

  @Mock private ArticleReadService articleReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  private ArticleQueryService articleQueryService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleQueryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
  }

  @Test
  public void should_find_article_by_id() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(articleReadService.findById("article-1")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(anyString(), anyString())).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(anyString())).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    User user = new User("test@example.com", "testuser", "password", "", "");
    Optional<ArticleData> result = articleQueryService.findById("article-1", user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getTitle(), is("Test Title"));
  }

  @Test
  public void should_return_empty_when_article_not_found() {
    when(articleReadService.findById("nonexistent")).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("nonexistent", null);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_find_article_by_slug() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(anyString(), anyString())).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(anyString())).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    User user = new User("test@example.com", "testuser", "password", "", "");
    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getSlug(), is("test-slug"));
  }

  @Test
  public void should_find_recent_articles() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("article-1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 5)));

    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertThat(result.getCount(), is(1));
    assertThat(result.getArticleDatas().size(), is(1));
  }

  @Test
  public void should_return_empty_list_when_no_articles() {
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertThat(result.getCount(), is(0));
    assertThat(result.getArticleDatas().size(), is(0));
  }

  @Test
  public void should_find_user_feed() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(userRelationshipQueryService.followedUsers(anyString()))
        .thenReturn(Arrays.asList("user-2"));
    when(articleReadService.findArticlesOfAuthors(anyList(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(anyList())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 5)));
    when(articleFavoritesReadService.userFavorites(anyList(), any())).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());

    User user = new User("test@example.com", "testuser", "password", "", "");
    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertThat(result.getCount(), is(1));
  }

  @Test
  public void should_return_empty_feed_when_not_following_anyone() {
    when(userRelationshipQueryService.followedUsers(anyString()))
        .thenReturn(Collections.emptyList());

    User user = new User("test@example.com", "testuser", "password", "", "");
    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertThat(result.getCount(), is(0));
    assertThat(result.getArticleDatas().size(), is(0));
  }

  @Test
  public void should_search_articles() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(articleReadService.searchArticles(anyString(), any()))
        .thenReturn(Arrays.asList("article-1"));
    when(articleReadService.countSearchResults(anyString())).thenReturn(1);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 5)));

    Page page = new Page(0, 10);
    ArticleDataList result = articleQueryService.searchArticles("test", page, null);

    assertThat(result.getCount(), is(1));
    assertThat(result.getArticleDatas().size(), is(1));
  }
}
