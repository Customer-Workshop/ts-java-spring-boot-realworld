package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class CommentStepDefinitions {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private CommentRepository commentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private SharedState sharedState;

  public CommentStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @When("the user adds a comment to article {string} with body {string}")
  public void theUserAddsACommentToArticleWithBody(String slug, String body) throws Exception {
    Map<String, Object> commentMap = new HashMap<>();
    commentMap.put("body", body);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("comment", commentMap);

    MvcResult result =
        mockMvc
            .perform(
                post("/articles/" + slug + "/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Token " + sharedState.getCurrentToken())
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);

    // Store comment ID if response was successful
    if (result.getResponse().getStatus() == 201) {
      String responseBody = result.getResponse().getContentAsString();
      JsonNode jsonNode = objectMapper.readTree(responseBody);
      if (jsonNode.has("comment") && jsonNode.get("comment").has("id")) {
        String commentId = jsonNode.get("comment").get("id").asText();
        // Store for potential later use in delete operations
        sharedState.setLastCreatedComment(
            commentRepository
                .findById(articleRepository.findBySlug(slug).get().getId(), commentId)
                .orElse(null));
      }
    }
  }

  @When("the user requests comments for article {string}")
  public void theUserRequestsCommentsForArticle(String slug) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles/" + slug + "/comments")
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles/" + slug + "/comments")).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("the response should contain a {string} wrapper object")
  public void theResponseShouldContainASingleWrapperObject(String wrapperName) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertThat(
        "Response should contain '" + wrapperName + "' wrapper",
        jsonNode.has(wrapperName),
        is(true));
    assertThat(
        "'" + wrapperName + "' should be an object",
        jsonNode.get(wrapperName).isObject(),
        is(true));
  }

  @Then("the response should contain a {string} wrapper array")
  public void theResponseShouldContainASingleWrapperArray(String wrapperName) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertThat(
        "Response should contain '" + wrapperName + "' wrapper",
        jsonNode.has(wrapperName),
        is(true));
    assertThat(
        "'" + wrapperName + "' should be an array", jsonNode.get(wrapperName).isArray(), is(true));
  }

  @Then("the comment body should be {string}")
  public void theCommentBodyShouldBe(String expectedBody) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode comment = jsonNode.get("comment");
    assertThat("comment should not be null", comment, notNullValue());
    assertThat(comment.get("body").asText(), equalTo(expectedBody));
  }

  @Then("the comment should have a valid ISO 8601 createdAt date")
  public void theCommentShouldHaveAValidISO8601CreatedAtDate() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode comment = jsonNode.get("comment");
    String createdAt = comment.get("createdAt").asText();
    assertThat("createdAt should not be null", createdAt, notNullValue());
    assertThat(
        "createdAt should be ISO 8601 format",
        Pattern.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z", createdAt),
        is(true));
  }

  @Then("the comment should have a valid ISO 8601 updatedAt date")
  public void theCommentShouldHaveAValidISO8601UpdatedAtDate() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode comment = jsonNode.get("comment");
    String updatedAt = comment.get("updatedAt").asText();
    assertThat("updatedAt should not be null", updatedAt, notNullValue());
    assertThat(
        "updatedAt should be ISO 8601 format",
        Pattern.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z", updatedAt),
        is(true));
  }

  @Then("the comment author username should be {string}")
  public void theCommentAuthorUsernameShouldBe(String username) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode comment = jsonNode.get("comment");
    JsonNode author = comment.get("author");
    assertThat("author should not be null", author, notNullValue());
    assertThat(author.get("username").asText(), equalTo(username));
  }

  @Then("the comments list should contain at least {int} comments")
  public void theCommentsListShouldContainAtLeastComments(int count) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode comments = jsonNode.get("comments");
    assertThat("comments should not be null", comments, notNullValue());
    assertThat(
        "comments count should be at least " + count, comments.size(), greaterThanOrEqualTo(count));
  }

  @Given("the article {string} has a comment {string} by {string}")
  public void theArticleHasACommentBy(String slug, String commentBody, String authorUsername)
      throws Exception {
    Article article = articleRepository.findBySlug(slug).orElseThrow();
    User author = userRepository.findByUsername(authorUsername).orElseThrow();
    Comment comment = new Comment(commentBody, author.getId(), article.getId());
    commentRepository.save(comment);
    sharedState.setLastCreatedComment(comment);
  }

  @When("the user deletes comment on article {string}")
  public void theUserDeletesCommentOnArticle(String slug) throws Exception {
    Comment comment = sharedState.getLastCreatedComment();
    assertThat("A comment should exist to delete", comment, notNullValue());

    MvcResult result =
        mockMvc
            .perform(
                delete("/articles/" + slug + "/comments/" + comment.getId())
                    .header("Authorization", "Token " + sharedState.getCurrentToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @When("the other user deletes comment on article {string}")
  public void theOtherUserDeletesCommentOnArticle(String slug) throws Exception {
    Comment comment = sharedState.getLastCreatedComment();
    assertThat("A comment should exist to delete", comment, notNullValue());

    MvcResult result =
        mockMvc
            .perform(
                delete("/articles/" + slug + "/comments/" + comment.getId())
                    .header("Authorization", "Token " + sharedState.getOtherToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }
}
