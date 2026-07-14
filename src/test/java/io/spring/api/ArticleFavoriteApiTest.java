package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * REST contract tests for the Article Favorites feature (issue #186).
 *
 * <p>Covers acceptance criteria AC1-AC10: POST /articles/{slug}/favorite and DELETE
 * /articles/{slug}/favorite — success, count reporting, idempotency/no-op, 404 for missing
 * articles, and 401 for unauthenticated requests.
 */
@WebMvcTest(ArticleFavoriteApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleFavoriteApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleQueryService articleQueryService;

  private Article article;
  private User anotherUser;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    anotherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
  }

  private ArticleData articleDataWith(boolean favorited, int favoritesCount) {
    return new ArticleData(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        favorited,
        favoritesCount,
        article.getCreatedAt(),
        article.getUpdatedAt(),
        article.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
        new ProfileData(
            anotherUser.getId(),
            anotherUser.getUsername(),
            anotherUser.getBio(),
            anotherUser.getImage(),
            false));
  }

  // AC1: authenticated user favoriting an existing article -> 200 and article.favorited == true.
  @Test
  public void should_favorite_article_returns_200_and_favorited_true() throws Exception {
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(true, 1)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true));

    verify(articleFavoriteRepository).save(any());
  }

  // AC2: the returned article.favoritesCount reflects the favorite count including this user.
  @Test
  public void should_favorite_article_returns_favorites_count() throws Exception {
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(true, 1)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favoritesCount", equalTo(1));
  }

  // AC3: favoriting an article that does not exist -> 404.
  @Test
  public void should_return_404_when_favoriting_missing_article() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", "not-exists")
        .prettyPeek()
        .then()
        .statusCode(404);

    verify(articleFavoriteRepository, never()).save(any());
  }

  // AC4: an unauthenticated favorite request -> 401 and does not change favorite state.
  @Test
  public void should_return_401_and_not_favorite_when_unauthenticated() throws Exception {
    given()
        .when()
        .post("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(401);

    verify(articleFavoriteRepository, never()).save(any());
  }

  // AC5: favoriting an already favorited article is idempotent -> still 200, favorited=true, and
  // the count is not double-incremented (count stays 1). The no-double-increment guarantee is
  // enforced by the repository/read-model and is additionally covered in
  // ArticleQueryServiceTest#should_not_double_count_when_same_user_favorites_twice.
  @Test
  public void should_be_idempotent_when_favoriting_already_favorited_article() throws Exception {
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(true, 1)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(true))
        .body("article.favoritesCount", equalTo(1));

    verify(articleFavoriteRepository).save(any());
  }

  // AC6: authenticated user unfavoriting a previously favorited article -> 200 and
  // article.favorited == false.
  @Test
  public void should_unfavorite_article_returns_200_and_favorited_false() throws Exception {
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(new ArticleFavorite(article.getId(), user.getId())));
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(false, 0)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(false));

    verify(articleFavoriteRepository).remove(new ArticleFavorite(article.getId(), user.getId()));
  }

  // AC7: article.favoritesCount decreases accordingly after unfavoriting (1 -> 0).
  @Test
  public void should_decrease_favorites_count_after_unfavorite() throws Exception {
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(new ArticleFavorite(article.getId(), user.getId())));
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(false, 0)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favoritesCount", equalTo(0));
  }

  // AC8: unfavoriting an article that does not exist -> 404.
  @Test
  public void should_return_404_when_unfavoriting_missing_article() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", "not-exists")
        .prettyPeek()
        .then()
        .statusCode(404);

    verify(articleFavoriteRepository, never()).remove(any());
  }

  // AC9: an unauthenticated unfavorite request -> 401.
  @Test
  public void should_return_401_when_unauthenticated_unfavorite() throws Exception {
    given()
        .when()
        .delete("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(401);

    verify(articleFavoriteRepository, never()).remove(any());
  }

  // AC10: unfavoriting an article the user had not favorited is a no-op -> 200, favorited=false,
  // count unchanged, and remove() is never invoked.
  @Test
  public void should_be_noop_when_unfavoriting_not_favorited_article() throws Exception {
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleDataWith(false, 2)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.favorited", equalTo(false))
        .body("article.favoritesCount", equalTo(2));

    verify(articleFavoriteRepository, never()).remove(any());
  }
}
