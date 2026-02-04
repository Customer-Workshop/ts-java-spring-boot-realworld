package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.ArticleData;
import io.spring.application.facade.ArticleApiFacade;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleApiFacade articleApiFacade;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_read_article_success() throws Exception {
    String slug = "test-new-article";
    DateTime time = new DateTime();
    Article article =
        new Article(
            "Test New Article",
            "Desc",
            "Body",
            Arrays.asList("java", "spring", "jpg"),
            user.getId(),
            time);
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

    when(articleApiFacade.getArticle(eq(slug), eq(null))).thenReturn(articleData);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}", slug)
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(slug))
        .body("article.body", equalTo(articleData.getBody()))
        .body("article.createdAt", equalTo(ISODateTimeFormat.dateTime().withZoneUTC().print(time)));
  }

  @Test
  public void should_404_if_article_not_found() throws Exception {
    when(articleApiFacade.getArticle(anyString(), any()))
        .thenThrow(new ResourceNotFoundException());
    RestAssuredMockMvc.when().get("/articles/not-exists").then().statusCode(404);
  }

  @Test
  public void should_update_article_content_success() throws Exception {
    List<String> tagList = Arrays.asList("java", "spring", "jpg");

    Article originalArticle =
        new Article("old title", "old description", "old body", tagList, user.getId());

    Article updatedArticle =
        new Article("new title", "new description", "new body", tagList, user.getId());

    Map<String, Object> updateParam =
        prepareUpdateParam(
            updatedArticle.getTitle(), updatedArticle.getBody(), updatedArticle.getDescription());

    ArticleData updatedArticleData =
        TestHelper.getArticleDataFromArticleAndUser(updatedArticle, user);

    when(articleApiFacade.updateArticle(eq(originalArticle.getSlug()), any(), any(User.class)))
        .thenReturn(updatedArticleData);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/articles/{slug}", originalArticle.getSlug())
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(updatedArticleData.getSlug()));
  }

  @Test
  public void should_get_403_if_not_author_to_update_article() throws Exception {
    String title = "new-title";
    String body = "new body";
    String description = "new description";
    Map<String, Object> updateParam = prepareUpdateParam(title, body, description);

    Article article =
        new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), "other-user");

    when(articleApiFacade.updateArticle(eq(article.getSlug()), any(), any(User.class)))
        .thenThrow(new NoAuthorizationException());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(403);
  }

  @Test
  public void should_delete_article_success() throws Exception {
    String title = "title";
    String body = "body";
    String description = "description";

    Article article =
        new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), user.getId());
    doNothing().when(articleApiFacade).deleteArticle(eq(article.getSlug()), any(User.class));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(204);

    verify(articleApiFacade).deleteArticle(eq(article.getSlug()), any(User.class));
  }

  @Test
  public void should_403_if_not_author_delete_article() throws Exception {
    String title = "new-title";
    String body = "new body";
    String description = "new description";

    Article article =
        new Article(title, description, body, Arrays.asList("java", "spring", "jpg"), "other-user");

    doThrow(new NoAuthorizationException())
        .when(articleApiFacade)
        .deleteArticle(eq(article.getSlug()), any(User.class));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(403);
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String title, final String body, final String description) {
    return new HashMap<String, Object>() {
      {
        put(
            "article",
            new HashMap<String, Object>() {
              {
                put("title", title);
                put("body", body);
                put("description", description);
              }
            });
      }
    };
  }
}
