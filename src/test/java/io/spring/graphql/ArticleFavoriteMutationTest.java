package io.spring.graphql;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * GraphQL parity tests for the Article Favorites feature (issue #186).
 *
 * <p>Covers AC11 and AC12: the favoriteArticle / unfavoriteArticle DGS mutations set the viewer's
 * `favorited` flag and return a `favoritesCount` that matches the REST read model ({@link
 * ArticleQueryService#findBySlug}) for the same user/article.
 */
@SpringBootTest(
    properties = {
      // SQLite in-memory DBs are per-connection; pin the pool to a single connection so the
      // schema and data written via repositories are visible to the DGS query executor.
      "spring.datasource.hikari.maximum-pool-size=1",
      "spring.datasource.hikari.minimum-idle=1"
    })
@ActiveProfiles("test")
@Transactional
public class ArticleFavoriteMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private ArticleQueryService articleQueryService;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("gql@test.com", "gqluser", "123", "", "");
    userRepository.save(user);
    article = new Article("gql title", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
    setCurrentUser(user);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setCurrentUser(User currentUser) {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  // AC11: favoriteArticle(slug) sets favorited=true and returns the updated favoritesCount,
  // matching the REST read model for the same user/article.
  @Test
  public void should_favorite_article_via_graphql_matching_rest() {
    Boolean favorited =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { favoriteArticle(slug: \""
                + article.getSlug()
                + "\") { article { favorited favoritesCount } } }",
            "data.favoriteArticle.article.favorited");
    Integer favoritesCount =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "query { article(slug: \"" + article.getSlug() + "\") { favorited favoritesCount } }",
            "data.article.favoritesCount");

    Assertions.assertTrue(favorited);
    Assertions.assertEquals(1, favoritesCount);

    ArticleData rest = articleQueryService.findBySlug(article.getSlug(), user).get();
    Assertions.assertTrue(rest.isFavorited());
    Assertions.assertEquals(rest.getFavoritesCount(), favoritesCount);
  }

  // AC12: unfavoriteArticle(slug) sets favorited=false, matching the REST read model.
  @Test
  public void should_unfavorite_article_via_graphql_matching_rest() {
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), user.getId()));

    Boolean favorited =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { unfavoriteArticle(slug: \""
                + article.getSlug()
                + "\") { article { favorited favoritesCount } } }",
            "data.unfavoriteArticle.article.favorited");
    Integer favoritesCount =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "query { article(slug: \"" + article.getSlug() + "\") { favorited favoritesCount } }",
            "data.article.favoritesCount");

    Assertions.assertFalse(favorited);
    Assertions.assertEquals(0, favoritesCount);

    ArticleData rest = articleQueryService.findBySlug(article.getSlug(), user).get();
    Assertions.assertFalse(rest.isFavorited());
    Assertions.assertEquals(rest.getFavoritesCount(), favoritesCount);
  }
}
