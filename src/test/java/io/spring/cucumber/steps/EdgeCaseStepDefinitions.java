package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class EdgeCaseStepDefinitions {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private final ObjectMapper responseMapper =
      new ObjectMapper().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

  private SharedState sharedState;

  public EdgeCaseStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @When("the user creates an article with empty tag list:")
  public void theUserCreatesAnArticleWithEmptyTagList(DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    Map<String, Object> articleMap = new HashMap<>();
    articleMap.put("title", data.get("title"));
    articleMap.put("description", data.get("description"));
    articleMap.put("body", data.get("body"));
    articleMap.put("tagList", new ArrayList<>());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", articleMap);

    MvcResult result =
        mockMvc
            .perform(
                post("/articles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Token " + sharedState.getCurrentToken())
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @Then("the article slug should not contain special characters")
  public void theArticleSlugShouldNotContainSpecialCharacters() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode article = jsonNode.get("article");
    String slug = article.get("slug").asText();
    // Slug should only contain lowercase alphanumeric characters and hyphens
    assertThat(
        "Slug should only contain lowercase alphanumeric characters and hyphens",
        Pattern.matches("[a-z0-9\\-]+", slug),
        is(true));
  }

  @Then("the article slug should use hyphens as separators")
  public void theArticleSlugShouldUseHyphensAsSeparators() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode article = jsonNode.get("article");
    String slug = article.get("slug").asText();
    assertThat("Slug should contain hyphens", slug.contains("-"), is(true));
    assertThat("Slug should not contain spaces", slug.contains(" "), is(false));
  }

  @Then("the article tagList should be an array")
  public void theArticleTagListShouldBeAnArray() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode article = jsonNode.get("article");
    JsonNode tagList = article.get("tagList");
    assertThat("tagList should not be null", tagList, notNullValue());
    assertThat("tagList should be an array", tagList.isArray(), is(true));
  }

  @When("an unauthenticated user creates an article with:")
  public void anUnauthenticatedUserCreatesAnArticleWith(DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    Map<String, Object> articleMap = new HashMap<>();
    articleMap.put("title", data.get("title"));
    articleMap.put("description", data.get("description"));
    articleMap.put("body", data.get("body"));

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", articleMap);

    MvcResult result =
        mockMvc
            .perform(
                post("/articles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @When("an unauthenticated user deletes the article with slug {string}")
  public void anUnauthenticatedUserDeletesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result = mockMvc.perform(delete("/articles/" + slug)).andReturn();
    sharedState.setLastResponse(result);
  }

  @When("an unauthenticated user favorites the article with slug {string}")
  public void anUnauthenticatedUserFavoritesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result = mockMvc.perform(post("/articles/" + slug + "/favorite")).andReturn();
    sharedState.setLastResponse(result);
  }

  @When("an unauthenticated user adds a comment to article {string}")
  public void anUnauthenticatedUserAddsACommentToArticle(String slug) throws Exception {
    Map<String, Object> commentMap = new HashMap<>();
    commentMap.put("body", "Unauthorized comment");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("comment", commentMap);

    MvcResult result =
        mockMvc
            .perform(
                post("/articles/" + slug + "/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @Then(
      "the article response should have fields: slug, title, description, body, tagList, createdAt, updatedAt, favorited, favoritesCount, author")
  public void theArticleResponseShouldHaveAllRequiredFields() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode article = jsonNode.get("article");
    assertThat("article should not be null", article, notNullValue());

    String[] requiredFields = {
      "slug",
      "title",
      "description",
      "body",
      "tagList",
      "createdAt",
      "updatedAt",
      "favorited",
      "favoritesCount",
      "author"
    };
    for (String field : requiredFields) {
      assertThat("Article should contain field '" + field + "'", article.has(field), is(true));
    }
  }

  @Then("the article author should have fields: username, bio, image, following")
  public void theArticleAuthorShouldHaveAllRequiredFields() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode author = jsonNode.get("article").get("author");
    assertThat("author should not be null", author, notNullValue());

    String[] requiredFields = {"username", "bio", "image", "following"};
    for (String field : requiredFields) {
      assertThat("Author should contain field '" + field + "'", author.has(field), is(true));
    }
  }
}
