package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Spec-driven acceptance tests for the REST comment endpoints, per feature spec issue #188. Covers
 * the acceptance criteria that were not already exercised by {@link CommentsApiTest}: AC2, AC4,
 * AC5, AC6 (ordering), AC8, AC9, AC10 (removal), AC12, AC13, plus the blank-body variant of AC3.
 */
@WebMvcTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsApiAcceptanceTest extends TestWithCurrentUser {

  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;

  @Autowired private MockMvc mvc;

  private Article article;
  private Comment comment;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    article = new Article("title", "desc", "body", Arrays.asList("test", "java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    comment = new Comment("comment", user.getId(), article.getId());
  }

  private CommentData commentDataFor(Comment c, boolean following) {
    return new CommentData(
        c.getId(),
        c.getBody(),
        c.getArticleId(),
        c.getCreatedAt(),
        c.getCreatedAt(),
        new ProfileData(
            user.getId(), user.getUsername(), user.getBio(), user.getImage(), following));
  }

  private Map<String, Object> commentPayload(String body) {
    return new HashMap<String, Object>() {
      {
        put(
            "comment",
            new HashMap<String, Object>() {
              {
                put("body", body);
              }
            });
      }
    };
  }

  // AC1 (body matches input) — asserted here alongside AC2 to keep the author assertions
  // self-contained.
  // AC2: create response includes comment.author (username, bio, image, following).
  @Test
  @DisplayName("AC1/AC2: create returns body matching input and full author profile")
  public void createComment_returnsAuthorProfile() throws Exception {
    CommentData created = commentDataFor(comment, false);
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(created));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(commentPayload(comment.getBody()))
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(201)
        .body("comment.body", equalTo(comment.getBody()))
        .body("comment.author.username", equalTo(user.getUsername()))
        .body("comment.author.bio", equalTo(user.getBio()))
        .body("comment.author.image", equalTo(user.getImage()))
        .body("comment.author.following", equalTo(false));
  }

  // AC3 (blank variant): a whitespace-only body is rejected with 422.
  @Test
  @DisplayName("AC3: blank (whitespace-only) comment body returns 422")
  public void createComment_blankBody_returns422() throws Exception {
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(commentPayload("   "))
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  // AC4: an unauthenticated create request returns 401.
  @Test
  @DisplayName("AC4: unauthenticated create returns 401")
  public void createComment_unauthenticated_returns401() throws Exception {
    given()
        .contentType("application/json")
        .body(commentPayload("comment content"))
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(401);
  }

  // AC5: commenting on a non-existent article returns 404.
  @Test
  @DisplayName("AC5: create on non-existent article returns 404")
  public void createComment_missingArticle_returns404() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(commentPayload("comment content"))
        .when()
        .post("/articles/{slug}/comments", "not-exists")
        .then()
        .statusCode(404);
  }

  // AC6: listing comments returns 200 with a comments array preserving creation order.
  @Test
  @DisplayName("AC6: list returns comments array in creation order")
  public void listComments_preservesCreationOrder() throws Exception {
    Comment first = new Comment("first", user.getId(), article.getId());
    Comment second = new Comment("second", user.getId(), article.getId());
    Comment third = new Comment("third", user.getId(), article.getId());
    when(commentQueryService.findByArticleId(anyString(), isNull()))
        .thenReturn(
            Arrays.asList(
                commentDataFor(first, false),
                commentDataFor(second, false),
                commentDataFor(third, false)));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(200)
        .body("comments", hasSize(3))
        .body("comments[0].id", equalTo(first.getId()))
        .body("comments[1].id", equalTo(second.getId()))
        .body("comments[2].id", equalTo(third.getId()));
  }

  // AC8: an anonymous viewer can list comments and sees author.following = false.
  @Test
  @DisplayName("AC8: anonymous viewer lists comments with following=false")
  public void listComments_anonymous_followingFalse() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), isNull()))
        .thenReturn(Arrays.asList(commentDataFor(comment, false)));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(200)
        .body("comments[0].author.following", equalTo(false));
  }

  // AC9: listing comments for a non-existent article returns 404.
  @Test
  @DisplayName("AC9: list on non-existent article returns 404")
  public void listComments_missingArticle_returns404() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/comments", "not-exists").then().statusCode(404);
  }

  // AC10: the comment author deleting their own comment returns 204 and the comment is removed.
  @Test
  @DisplayName("AC10: author deletes own comment -> 204 and removed")
  public void deleteComment_byAuthor_returns204AndRemoves() throws Exception {
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .then()
        .statusCode(204);

    verify(commentRepository).remove(eq(comment));
  }

  // AC12: an unauthenticated delete request returns 401.
  @Test
  @DisplayName("AC12: unauthenticated delete returns 401")
  public void deleteComment_unauthenticated_returns401() throws Exception {
    given()
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .then()
        .statusCode(401);

    verify(commentRepository, never()).remove(any());
  }

  // AC13: deleting a non-existent comment id returns 404.
  @Test
  @DisplayName("AC13: delete of non-existent comment id returns 404")
  public void deleteComment_missingComment_returns404() throws Exception {
    when(commentRepository.findById(eq(article.getId()), anyString())).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), "no-such-id")
        .then()
        .statusCode(404);
  }
}
