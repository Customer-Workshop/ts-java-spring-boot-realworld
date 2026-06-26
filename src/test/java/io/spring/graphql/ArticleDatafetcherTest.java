package io.spring.graphql;

import static io.spring.TestHelper.articleDataFixture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ArticleDatafetcherTest {
  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private MockedStatic<SecurityUtil> securityUtil;
  private User user;

  @BeforeEach
  public void setUp() {
    articleQueryService = Mockito.mock(ArticleQueryService.class);
    userRepository = Mockito.mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("user@example.com", "user", "123", "", "");
    securityUtil = mockStatic(SecurityUtil.class);
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
  }

  @AfterEach
  public void tearDown() {
    securityUtil.close();
  }

  @Test
  public void should_expose_bookmarked_true_on_single_article() {
    ArticleData articleData = articleDataFixture("1", user);
    articleData.setBookmarked(true);
    when(articleQueryService.findBySlug(eq(articleData.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug(articleData.getSlug());

    Assertions.assertTrue(result.getData().getBookmarked());
  }

  @Test
  public void should_expose_bookmarked_false_for_anonymous_user() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    ArticleData articleData = articleDataFixture("1", user);
    when(articleQueryService.findBySlug(eq(articleData.getSlug()), eq(null)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug(articleData.getSlug());

    Assertions.assertFalse(result.getData().getBookmarked());
  }

  @Test
  public void should_expose_bookmarked_flag_in_article_list() {
    ArticleData bookmarked = articleDataFixture("1", user);
    bookmarked.setBookmarked(true);
    ArticleData notBookmarked = articleDataFixture("2", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(bookmarked, notBookmarked), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, null);

    Assertions.assertTrue(result.getData().getEdges().get(0).getNode().getBookmarked());
    Assertions.assertFalse(result.getData().getEdges().get(1).getNode().getBookmarked());
  }

  @Test
  public void should_expose_bookmarked_flag_in_feed() {
    ArticleData bookmarked = articleDataFixture("1", user);
    bookmarked.setBookmarked(true);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(bookmarked), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, null);

    Assertions.assertTrue(result.getData().getEdges().get(0).getNode().getBookmarked());
  }
}
