package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class ArticleCrudStepDefinitions {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private final ObjectMapper responseMapper =
      new ObjectMapper().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private JwtService jwtService;

  @Autowired private PasswordEncoder passwordEncoder;

  private SharedState sharedState;

  public ArticleCrudStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @Before
  public void setUp() {
    sharedState.reset();
  }

  @Given("a registered user exists with username {string} and email {string}")
  public void aRegisteredUserExistsWithUsernameAndEmail(String username, String email) {
    Optional<User> existingUser = userRepository.findByUsername(username);
    if (existingUser.isPresent()) {
      sharedState.setCurrentUser(existingUser.get());
    } else {
      User user =
          new User(
              email, username, passwordEncoder.encode("password123"), "test bio", "test image");
      userRepository.save(user);
      sharedState.setCurrentUser(user);
    }
  }

  @Given("the user has a valid authentication token")
  public void theUserHasAValidAuthenticationToken() {
    String token = jwtService.toToken(sharedState.getCurrentUser());
    sharedState.setCurrentToken(token);
  }

  @When("the user creates an article with:")
  public void theUserCreatesAnArticleWith(DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    Map<String, Object> articleMap = new HashMap<>();
    articleMap.put("title", data.get("title"));
    articleMap.put("description", data.get("description"));
    articleMap.put("body", data.get("body"));

    if (data.containsKey("tagList")) {
      articleMap.put("tagList", Arrays.asList(data.get("tagList").split(",")));
    } else {
      articleMap.put("tagList", Collections.emptyList());
    }

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

  @Given("an article exists with title {string} by user {string}")
  public void anArticleExistsWithTitleByUser(String title, String authorUsername) throws Exception {
    User author;
    Optional<User> existingAuthor = userRepository.findByUsername(authorUsername);
    if (existingAuthor.isPresent()) {
      author = existingAuthor.get();
    } else {
      author =
          new User(
              authorUsername + "@example.com",
              authorUsername,
              passwordEncoder.encode("password123"),
              "",
              "");
      userRepository.save(author);
    }

    String slug = Article.toSlug(title);
    Optional<Article> existingArticle = articleRepository.findBySlug(slug);
    Article article;
    if (existingArticle.isPresent()) {
      article = existingArticle.get();
    } else {
      article =
          new Article(
              title, "Test description", "Test body", Collections.emptyList(), author.getId());
      articleRepository.save(article);
    }
    sharedState.setLastCreatedArticle(article);
  }

  @When("the user requests the article with slug {string}")
  public void theUserRequestsTheArticleWithSlug(String slug) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles/" + slug)
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles/" + slug)).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @When("the user updates the article {string} with:")
  public void theUserUpdatesTheArticleWith(String slug, DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    Map<String, Object> articleMap = new HashMap<>();
    if (data.containsKey("title")) {
      articleMap.put("title", data.get("title"));
    }
    if (data.containsKey("body")) {
      articleMap.put("body", data.get("body"));
    }
    if (data.containsKey("description")) {
      articleMap.put("description", data.get("description"));
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", articleMap);

    MvcResult result =
        mockMvc
            .perform(
                put("/articles/" + slug)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Token " + sharedState.getCurrentToken())
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @Given("a different user {string} is authenticated")
  public void aDifferentUserIsAuthenticated(String username) {
    Optional<User> existingUser = userRepository.findByUsername(username);
    User otherUser;
    if (existingUser.isPresent()) {
      otherUser = existingUser.get();
    } else {
      otherUser =
          new User(
              username + "@example.com", username, passwordEncoder.encode("password123"), "", "");
      userRepository.save(otherUser);
    }
    String otherToken = jwtService.toToken(otherUser);
    sharedState.setOtherUser(otherUser);
    sharedState.setOtherToken(otherToken);
  }

  @When("the other user updates the article {string} with:")
  public void theOtherUserUpdatesTheArticleWith(String slug, DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    Map<String, Object> articleMap = new HashMap<>();
    if (data.containsKey("title")) {
      articleMap.put("title", data.get("title"));
    }
    if (data.containsKey("body")) {
      articleMap.put("body", data.get("body"));
    }
    if (data.containsKey("description")) {
      articleMap.put("description", data.get("description"));
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", articleMap);

    MvcResult result =
        mockMvc
            .perform(
                put("/articles/" + slug)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Token " + sharedState.getOtherToken())
                    .content(objectMapper.writeValueAsString(requestBody)))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @When("the user deletes the article with slug {string}")
  public void theUserDeletesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                delete("/articles/" + slug)
                    .header("Authorization", "Token " + sharedState.getCurrentToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @When("the other user deletes the article with slug {string}")
  public void theOtherUserDeletesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                delete("/articles/" + slug)
                    .header("Authorization", "Token " + sharedState.getOtherToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  // --- Response Assertions ---

  @Then("the response status code should be {int}")
  public void theResponseStatusCodeShouldBe(int statusCode) {
    assertThat(sharedState.getLastResponse().getResponse().getStatus(), is(statusCode));
  }

  @Then("the response should contain an {string} wrapper object")
  public void theResponseShouldContainAWrapperObject(String wrapperName) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    assertThat(
        "Response should contain '" + wrapperName + "' wrapper",
        jsonNode.has(wrapperName),
        is(true));
    assertThat(
        "'" + wrapperName + "' should be an object",
        jsonNode.get(wrapperName).isObject(),
        is(true));
  }

  @Then("the article title should be {string}")
  public void theArticleTitleShouldBe(String expectedTitle) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("title").asText(), equalTo(expectedTitle));
  }

  @Then("the article description should be {string}")
  public void theArticleDescriptionShouldBe(String expectedDescription) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("description").asText(), equalTo(expectedDescription));
  }

  @Then("the article body should be {string}")
  public void theArticleBodyShouldBe(String expectedBody) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("body").asText(), equalTo(expectedBody));
  }

  @Then("the article slug should be {string}")
  public void theArticleSlugShouldBe(String expectedSlug) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("slug").asText(), equalTo(expectedSlug));
  }

  @Then("the article tagList should contain {string}")
  public void theArticleTagListShouldContain(String tag) throws Exception {
    JsonNode article = getArticleFromResponse();
    JsonNode tagList = article.get("tagList");
    assertThat("tagList should not be null", tagList, notNullValue());
    boolean found = false;
    for (JsonNode tagNode : tagList) {
      if (tagNode.asText().equals(tag)) {
        found = true;
        break;
      }
    }
    assertThat("tagList should contain '" + tag + "'", found, is(true));
  }

  @Then("the article tagList should be empty")
  public void theArticleTagListShouldBeEmpty() throws Exception {
    JsonNode article = getArticleFromResponse();
    JsonNode tagList = article.get("tagList");
    assertThat("tagList should not be null", tagList, notNullValue());
    assertThat("tagList should be empty", tagList.size(), is(0));
  }

  @Then("the article should have a valid ISO 8601 createdAt date")
  public void theArticleShouldHaveAValidISO8601CreatedAtDate() throws Exception {
    JsonNode article = getArticleFromResponse();
    String createdAt = article.get("createdAt").asText();
    assertThat("createdAt should not be null", createdAt, notNullValue());
    assertThat(
        "createdAt should be ISO 8601 format",
        Pattern.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z", createdAt),
        is(true));
  }

  @Then("the article should have a valid ISO 8601 updatedAt date")
  public void theArticleShouldHaveAValidISO8601UpdatedAtDate() throws Exception {
    JsonNode article = getArticleFromResponse();
    String updatedAt = article.get("updatedAt").asText();
    assertThat("updatedAt should not be null", updatedAt, notNullValue());
    assertThat(
        "updatedAt should be ISO 8601 format",
        Pattern.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z", updatedAt),
        is(true));
  }

  @Then("the article author username should be {string}")
  public void theArticleAuthorUsernameShouldBe(String username) throws Exception {
    JsonNode article = getArticleFromResponse();
    JsonNode author = article.get("author");
    assertThat("author should not be null", author, notNullValue());
    assertThat(author.get("username").asText(), equalTo(username));
  }

  @Then("the article favorited should be {}")
  public void theArticleFavoritedShouldBe(boolean favorited) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("favorited").asBoolean(), is(favorited));
  }

  @Then("the article favoritesCount should be {int}")
  public void theArticleFavoritesCountShouldBe(int count) throws Exception {
    JsonNode article = getArticleFromResponse();
    assertThat(article.get("favoritesCount").asInt(), is(count));
  }

  private JsonNode getArticleFromResponse() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    return jsonNode.get("article");
  }
}
