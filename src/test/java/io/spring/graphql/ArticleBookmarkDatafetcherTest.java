package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.TestHelper;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.BookmarkedArticleData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticleEdge;
import io.spring.graphql.types.ArticlesConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleBookmarkDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private UserRepository userRepository;

  private ArticleDatafetcher articleDatafetcher;

  private User user;

  @BeforeEach
  public void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("reader@example.com", "reader", "123", "", "");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate(User current) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(current, null, Collections.emptyList()));
  }

  private void anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_build_connection_with_bookmark_time_cursors() {
    authenticate(user);

    ArticleData a1 = TestHelper.articleDataFixture("1", user);
    ArticleData a2 = TestHelper.articleDataFixture("2", user);
    DateTime t1 = new DateTime("2021-05-01T10:00:02Z");
    DateTime t2 = new DateTime("2021-05-01T10:00:01Z");
    List<BookmarkedArticleData> data =
        Arrays.asList(new BookmarkedArticleData(a1, t1), new BookmarkedArticleData(a2, t2));
    CursorPager<BookmarkedArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    when(articleQueryService.findUserBookmarksWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getBookmarkedArticles(10, null, null, null, null);

    ArticlesConnection connection = result.getData();
    Assertions.assertEquals(2, connection.getEdges().size());

    ArticleEdge firstEdge = connection.getEdges().get(0);
    Assertions.assertEquals(String.valueOf(t1.getMillis()), firstEdge.getCursor());
    Assertions.assertEquals(a1.getSlug(), firstEdge.getNode().getSlug());
    Assertions.assertEquals(
        String.valueOf(t2.getMillis()), connection.getEdges().get(1).getCursor());

    Assertions.assertTrue(connection.getPageInfo().isHasNextPage());
    Assertions.assertFalse(connection.getPageInfo().isHasPreviousPage());
    Assertions.assertEquals(
        String.valueOf(t2.getMillis()), connection.getPageInfo().getEndCursor().toString());

    // localContext is keyed by slug -> ArticleData, like the other article connections.
    @SuppressWarnings("unchecked")
    java.util.Map<String, ArticleData> localContext =
        (java.util.Map<String, ArticleData>) result.getLocalContext();
    Assertions.assertTrue(localContext.containsKey(a1.getSlug()));
    Assertions.assertTrue(localContext.containsKey(a2.getSlug()));
  }

  @Test
  public void should_use_last_before_for_backward_paging() {
    authenticate(user);

    CursorPager<BookmarkedArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findUserBookmarksWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getBookmarkedArticles(null, null, 10, null, null);

    Assertions.assertNotNull(result.getData());
    Assertions.assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  public void should_throw_when_anonymous() {
    anonymous();
    Assertions.assertThrows(
        AuthenticationException.class,
        () -> articleDatafetcher.getBookmarkedArticles(10, null, null, null, null));
  }

  @Test
  public void should_require_first_or_last() {
    authenticate(user);
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getBookmarkedArticles(null, null, null, null, null));
  }
}
