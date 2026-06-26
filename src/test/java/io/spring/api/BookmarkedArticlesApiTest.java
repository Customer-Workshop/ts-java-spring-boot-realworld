package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticlesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class BookmarkedArticlesApiTest extends TestWithCurrentUser {
  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Autowired private MockMvc mvc;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_401_without_login() throws Exception {
    RestAssuredMockMvc.when().get("/articles/bookmarked").prettyPeek().then().statusCode(401);
  }

  @Test
  public void should_get_bookmarked_articles_with_envelope() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findUserBookmarks(eq(user), eq(new Page(0, 20))))
        .thenReturn(articleDataList);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/bookmarked")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", org.hamcrest.Matchers.is(2))
        .body("articles.size()", org.hamcrest.Matchers.is(2));
  }

  @Test
  public void should_pass_offset_and_limit_through() throws Exception {
    when(articleQueryService.findUserBookmarks(eq(user), eq(new Page(5, 10))))
        .thenReturn(new ArticleDataList(asList(articleDataFixture("1", user)), 11));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/bookmarked?offset=5&limit=10")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", org.hamcrest.Matchers.is(11))
        .body("articles.size()", org.hamcrest.Matchers.is(1));
  }
}
