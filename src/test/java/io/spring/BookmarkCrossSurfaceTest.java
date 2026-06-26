package io.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * MTM-5 / MTM-23: cross-surface equivalence and end-to-end coverage for the reading-list (bookmark)
 * feature. Drives the SAME application stack (real repositories + DB) through BOTH the REST
 * controllers (via {@link MockMvc}) and the GraphQL adapter (via {@link DgsQueryExecutor}) and
 * asserts that the two surfaces expose the same underlying data, membership, ordering, auth/privacy
 * rules, the per-article {@code bookmarked} flag, and independence from favorites.
 *
 * <p>The intentional pagination-mechanics divergence (D4: REST offset/limit vs GraphQL Relay
 * cursor) is honored: equivalence is asserted on the resulting set and newest-bookmarked-first
 * ordering, not on the paging idiom. Ordering is made deterministic by stamping controlled bookmark
 * {@code created_at} values after each write.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class BookmarkCrossSurfaceTest {

  @Autowired private MockMvc mvc;
  @Autowired private DgsQueryExecutor dgsQueryExecutor;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private JwtService jwtService;
  @Autowired private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;
  private User user;
  private User other;
  private String token;
  private String otherToken;
  private Article a1;
  private Article a2;
  private Article a3;

  @BeforeEach
  public void setUp() {
    jdbcTemplate = new JdbcTemplate(dataSource);
    // V3 migration is skipped under the test profile (flyway.target=1); create the table here,
    // mirroring the existing bookmark infrastructure tests.
    jdbcTemplate.execute(
        "create table if not exists article_bookmarks ("
            + "article_id varchar(255) not null,"
            + "user_id varchar(255) not null,"
            + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "primary key (article_id, user_id))");

    user = new User("e2e@test.com", "e2euser", "123", "", "");
    other = new User("other@test.com", "otheruser", "123", "", "");
    userRepository.save(user);
    userRepository.save(other);
    token = jwtService.toToken(user);
    otherToken = jwtService.toToken(other);

    a1 = new Article("Title One", "desc one", "body one", Arrays.asList("java"), user.getId());
    a2 = new Article("Title Two", "desc two", "body two", Arrays.asList("kotlin"), user.getId());
    a3 =
        new Article(
            "Title Three", "desc three", "body three", Arrays.asList("spring"), user.getId());
    articleRepository.save(a1);
    articleRepository.save(a2);
    articleRepository.save(a3);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // ---- Cross-surface E2E ---------------------------------------------------

  @Test
  public void bookmark_via_rest_is_visible_via_graphql_and_removable() throws Exception {
    restPost("/articles/" + a1.getSlug() + "/bookmark", token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.article.bookmarked").value(true));
    setBookmarkTime(a1, user, "2020-01-01 00:00:01");

    // GraphQL sees the per-article flag and the reading-list membership.
    Assertions.assertTrue(graphqlArticleBookmarked(a1, user));
    Assertions.assertEquals(Collections.singletonList(a1.getSlug()), graphqlBookmarkedSlugs(user));

    // Remove via REST -> disappears on GraphQL.
    restDelete("/articles/" + a1.getSlug() + "/bookmark", token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.article.bookmarked").value(false));
    Assertions.assertFalse(graphqlArticleBookmarked(a1, user));
    Assertions.assertTrue(graphqlBookmarkedSlugs(user).isEmpty());
  }

  @Test
  public void bookmark_via_graphql_is_visible_via_rest_and_removable() throws Exception {
    graphqlMutation("bookmarkArticle", a2.getSlug(), user);
    setBookmarkTime(a2, user, "2020-01-01 00:00:02");

    // REST reading list and REST per-article flag both see it.
    Assertions.assertEquals(Collections.singletonList(a2.getSlug()), restBookmarkedSlugs(token));
    restGet("/articles/" + a2.getSlug(), token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.article.bookmarked").value(true));

    // Remove via GraphQL -> disappears on REST.
    graphqlMutation("unbookmarkArticle", a2.getSlug(), user);
    Assertions.assertTrue(restBookmarkedSlugs(token).isEmpty());
    restGet("/articles/" + a2.getSlug(), token)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.article.bookmarked").value(false));
  }

  // ---- Equivalence: same set + same newest-first ordering -----------------

  @Test
  public void both_surfaces_return_same_set_and_newest_first_ordering() throws Exception {
    // Mix the write surfaces; ordering must depend only on bookmark time, not on how it was added.
    restPost("/articles/" + a1.getSlug() + "/bookmark", token).andExpect(status().isOk());
    graphqlMutation("bookmarkArticle", a2.getSlug(), user);
    restPost("/articles/" + a3.getSlug() + "/bookmark", token).andExpect(status().isOk());

    setBookmarkTime(a1, user, "2020-01-01 00:00:01");
    setBookmarkTime(a2, user, "2020-01-01 00:00:02");
    setBookmarkTime(a3, user, "2020-01-01 00:00:03");

    List<String> expected = Arrays.asList(a3.getSlug(), a2.getSlug(), a1.getSlug());
    List<String> restSlugs = restBookmarkedSlugs(token);
    List<String> graphqlSlugs = graphqlBookmarkedSlugs(user);

    Assertions.assertEquals(expected, restSlugs, "REST reading list must be newest-first");
    Assertions.assertEquals(expected, graphqlSlugs, "GraphQL reading list must be newest-first");
    Assertions.assertEquals(restSlugs, graphqlSlugs, "REST and GraphQL must return the same order");
  }

  // ---- Auth / privacy on both surfaces ------------------------------------

  @Test
  public void anonymous_is_rejected_on_both_surfaces() throws Exception {
    // REST: 401 for list and add.
    restGet("/articles/bookmarked", null).andExpect(status().isUnauthorized());
    restPost("/articles/" + a1.getSlug() + "/bookmark", null).andExpect(status().isUnauthorized());

    // GraphQL: AuthenticationException -> errors present, no data leaked.
    anonymousContext();
    Assertions.assertFalse(
        dgsQueryExecutor
            .execute("query { bookmarkedArticles(first: 10) { edges { node { slug } } } }")
            .getErrors()
            .isEmpty());
    Assertions.assertFalse(
        dgsQueryExecutor
            .execute(
                "mutation { bookmarkArticle(slug: \"" + a1.getSlug() + "\") { article { slug } } }")
            .getErrors()
            .isEmpty());
  }

  @Test
  public void one_user_never_sees_another_users_bookmarks_on_either_surface() throws Exception {
    // 'other' bookmarks a1; 'user' has nothing.
    restPost("/articles/" + a1.getSlug() + "/bookmark", otherToken).andExpect(status().isOk());
    setBookmarkTime(a1, other, "2020-01-01 00:00:05");

    // REST reading list for 'user' is empty.
    Assertions.assertTrue(restBookmarkedSlugs(token).isEmpty());
    // GraphQL reading list for 'user' is empty.
    Assertions.assertTrue(graphqlBookmarkedSlugs(user).isEmpty());
    // Per-article flag is per-user on both surfaces.
    Assertions.assertFalse(graphqlArticleBookmarked(a1, user));
    restGet("/articles/" + a1.getSlug(), token)
        .andExpect(jsonPath("$.article.bookmarked").value(false));
    // 'other' still sees their own bookmark.
    Assertions.assertEquals(
        Collections.singletonList(a1.getSlug()), restBookmarkedSlugs(otherToken));
  }

  // ---- Independence from favorites on both surfaces -----------------------

  @Test
  public void bookmarking_does_not_change_favorite_state_on_either_surface() throws Exception {
    restPost("/articles/" + a1.getSlug() + "/bookmark", token)
        .andExpect(jsonPath("$.article.bookmarked").value(true))
        .andExpect(jsonPath("$.article.favorited").value(false))
        .andExpect(jsonPath("$.article.favoritesCount").value(0));
    setBookmarkTime(a1, user, "2020-01-01 00:00:01");

    Assertions.assertTrue(graphqlArticleBookmarked(a1, user));
    Assertions.assertFalse(graphqlArticleFavorited(a1, user));
    Assertions.assertEquals(0, graphqlArticleFavoritesCount(a1, user));
  }

  @Test
  public void favoriting_does_not_change_bookmark_state_on_either_surface() throws Exception {
    restPost("/articles/" + a2.getSlug() + "/favorite", token)
        .andExpect(jsonPath("$.article.favorited").value(true))
        .andExpect(jsonPath("$.article.bookmarked").value(false));

    // Favorited article must not appear in the reading list on either surface...
    Assertions.assertTrue(restBookmarkedSlugs(token).isEmpty());
    Assertions.assertTrue(graphqlBookmarkedSlugs(user).isEmpty());
    // ...and the per-article bookmarked flag stays false while favorited stays true.
    Assertions.assertFalse(graphqlArticleBookmarked(a2, user));
    Assertions.assertTrue(graphqlArticleFavorited(a2, user));
  }

  // ---- helpers ------------------------------------------------------------

  private void setBookmarkTime(Article article, User owner, String createdAt) {
    int rows =
        jdbcTemplate.update(
            "update article_bookmarks set created_at = ? where article_id = ? and user_id = ?",
            createdAt,
            article.getId(),
            owner.getId());
    Assertions.assertEquals(1, rows, "expected exactly one bookmark row to stamp");
  }

  private org.springframework.test.web.servlet.ResultActions restPost(String path, String token)
      throws Exception {
    SecurityContextHolder.clearContext();
    var req = post(path);
    if (token != null) {
      req = req.header("Authorization", "Token " + token);
    }
    return mvc.perform(req);
  }

  private org.springframework.test.web.servlet.ResultActions restDelete(String path, String token)
      throws Exception {
    SecurityContextHolder.clearContext();
    var req = delete(path);
    if (token != null) {
      req = req.header("Authorization", "Token " + token);
    }
    return mvc.perform(req);
  }

  private org.springframework.test.web.servlet.ResultActions restGet(String path, String token)
      throws Exception {
    SecurityContextHolder.clearContext();
    var req = get(path);
    if (token != null) {
      req = req.header("Authorization", "Token " + token);
    }
    return mvc.perform(req);
  }

  private List<String> restBookmarkedSlugs(String token) throws Exception {
    String body =
        restGet("/articles/bookmarked", token)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(body, "$.articles[*].slug");
  }

  private void authenticate(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList()));
  }

  private void anonymousContext() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void graphqlMutation(String field, String slug, User as) {
    authenticate(as);
    String returnedSlug =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { " + field + "(slug: \"" + slug + "\") { article { slug } } }",
            "data." + field + ".article.slug");
    Assertions.assertEquals(slug, returnedSlug);
  }

  private List<String> graphqlBookmarkedSlugs(User as) {
    authenticate(as);
    return dgsQueryExecutor.executeAndExtractJsonPath(
        "query { bookmarkedArticles(first: 10) { edges { node { slug } } } }",
        "data.bookmarkedArticles.edges[*].node.slug");
  }

  private boolean graphqlArticleBookmarked(Article article, User as) {
    authenticate(as);
    return dgsQueryExecutor.executeAndExtractJsonPath(
        "query { article(slug: \"" + article.getSlug() + "\") { bookmarked } }",
        "data.article.bookmarked");
  }

  private boolean graphqlArticleFavorited(Article article, User as) {
    authenticate(as);
    return dgsQueryExecutor.executeAndExtractJsonPath(
        "query { article(slug: \"" + article.getSlug() + "\") { favorited } }",
        "data.article.favorited");
  }

  private int graphqlArticleFavoritesCount(Article article, User as) {
    authenticate(as);
    return dgsQueryExecutor.executeAndExtractJsonPath(
        "query { article(slug: \"" + article.getSlug() + "\") { favoritesCount } }",
        "data.article.favoritesCount");
  }
}
