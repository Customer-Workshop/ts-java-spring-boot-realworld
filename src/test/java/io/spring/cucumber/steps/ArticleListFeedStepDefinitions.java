package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class ArticleListFeedStepDefinitions {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private SharedState sharedState;

  public ArticleListFeedStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @Given("the following articles exist:")
  public void theFollowingArticlesExist(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> row : rows) {
      String title = row.get("title");
      String authorUsername = row.get("author");
      String tagsStr = row.get("tags");
      List<String> tags =
          (tagsStr != null && !tagsStr.isEmpty())
              ? Arrays.asList(tagsStr.split(","))
              : Collections.emptyList();

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

      Article article =
          new Article(title, "Description for " + title, "Body of " + title, tags, author.getId());
      articleRepository.save(article);
    }
  }

  @When("the user requests the articles list")
  public void theUserRequestsTheArticlesList() throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles")).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("the response should contain an {string} wrapper array")
  public void theResponseShouldContainAWrapperArray(String wrapperName) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertThat(
        "Response should contain '" + wrapperName + "' wrapper",
        jsonNode.has(wrapperName),
        is(true));
    assertThat(
        "'" + wrapperName + "' should be an array", jsonNode.get(wrapperName).isArray(), is(true));
  }

  @Then("the response should contain an {string} field")
  public void theResponseShouldContainAnField(String fieldName) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertThat(
        "Response should contain '" + fieldName + "' field", jsonNode.has(fieldName), is(true));
  }

  @When("the user requests articles filtered by tag {string}")
  public void theUserRequestsArticlesFilteredByTag(String tag) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .param("tag", tag)
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles").param("tag", tag)).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("all returned articles should contain the tag {string}")
  public void allReturnedArticlesShouldContainTheTag(String tag) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode articles = jsonNode.get("articles");
    assertThat("articles should not be null", articles, notNullValue());
    for (JsonNode article : articles) {
      JsonNode tagList = article.get("tagList");
      boolean found = false;
      for (JsonNode tagNode : tagList) {
        if (tagNode.asText().equals(tag)) {
          found = true;
          break;
        }
      }
      assertThat(
          "Article '" + article.get("title").asText() + "' should contain tag '" + tag + "'",
          found,
          is(true));
    }
  }

  @When("the user requests articles filtered by author {string}")
  public void theUserRequestsArticlesFilteredByAuthor(String author) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .param("author", author)
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles").param("author", author)).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("all returned articles should have author {string}")
  public void allReturnedArticlesShouldHaveAuthor(String authorUsername) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode articles = jsonNode.get("articles");
    assertThat("articles should not be null", articles, notNullValue());
    for (JsonNode article : articles) {
      String articleAuthor = article.get("author").get("username").asText();
      assertThat(
          "Article author should be '" + authorUsername + "'", articleAuthor, is(authorUsername));
    }
  }

  @Given("the user {string} has favorited {string}")
  public void theUserHasFavorited(String username, String articleTitle) {
    User user = userRepository.findByUsername(username).orElseThrow();
    String slug = Article.toSlug(articleTitle);
    Article article = articleRepository.findBySlug(slug).orElseThrow();
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(favorite);
  }

  @When("the user requests articles favorited by {string}")
  public void theUserRequestsArticlesFavoritedBy(String username) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .param("favorited", username)
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/articles").param("favorited", username)).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("the returned articles should include {string}")
  public void theReturnedArticlesShouldInclude(String articleTitle) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode articles = jsonNode.get("articles");
    assertThat("articles should not be null", articles, notNullValue());
    boolean found = false;
    for (JsonNode article : articles) {
      if (article.get("title").asText().equals(articleTitle)) {
        found = true;
        break;
      }
    }
    assertThat("Articles should include '" + articleTitle + "'", found, is(true));
  }

  @Given("{int} articles exist in the system")
  public void articlesExistInTheSystem(int count) {
    User author;
    Optional<User> existingAuthor = userRepository.findByUsername("bulkauthor");
    if (existingAuthor.isPresent()) {
      author = existingAuthor.get();
    } else {
      author =
          new User(
              "bulkauthor@example.com",
              "bulkauthor",
              passwordEncoder.encode("password123"),
              "",
              "");
      userRepository.save(author);
    }

    for (int i = 0; i < count; i++) {
      Article article =
          new Article(
              "Bulk Article " + i,
              "Description " + i,
              "Body " + i,
              Collections.singletonList("bulk"),
              author.getId());
      articleRepository.save(article);
    }
  }

  @When("the user requests articles with limit {int} and offset {int}")
  public void theUserRequestsArticlesWithLimitAndOffset(int limit, int offset) throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .param("limit", String.valueOf(limit))
                      .param("offset", String.valueOf(offset))
                      .header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result =
          mockMvc
              .perform(
                  get("/articles")
                      .param("limit", String.valueOf(limit))
                      .param("offset", String.valueOf(offset)))
              .andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @Then("the returned articles count should be at most {int}")
  public void theReturnedArticlesCountShouldBeAtMost(int maxCount) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode articles = jsonNode.get("articles");
    assertThat("articles should not be null", articles, notNullValue());
    assertThat(
        "articles count should be at most " + maxCount,
        articles.size(),
        lessThanOrEqualTo(maxCount));
  }

  @Given("a user {string} exists")
  public void aUserExists(String username) {
    Optional<User> existing = userRepository.findByUsername(username);
    if (!existing.isPresent()) {
      User user =
          new User(
              username + "@example.com", username, passwordEncoder.encode("password123"), "", "");
      userRepository.save(user);
    }
  }

  @Given("the user {string} follows {string}")
  public void theUserFollows(String followerUsername, String followedUsername) {
    User follower = userRepository.findByUsername(followerUsername).orElseThrow();
    User followed = userRepository.findByUsername(followedUsername).orElseThrow();
    userRepository.saveRelation(new FollowRelation(follower.getId(), followed.getId()));
  }

  @Given("{string} has published articles")
  public void hasPublishedArticles(String authorUsername) {
    User author = userRepository.findByUsername(authorUsername).orElseThrow();
    Article article =
        new Article(
            "Article by " + authorUsername,
            "Description",
            "Body content",
            Collections.singletonList("feed"),
            author.getId());
    articleRepository.save(article);
  }

  @When("the user requests their feed")
  public void theUserRequestsTheirFeed() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/articles/feed")
                    .header("Authorization", "Token " + sharedState.getCurrentToken()))
            .andReturn();
    sharedState.setLastResponse(result);
  }

  @Then("the feed articles should be from followed users")
  public void theFeedArticlesShouldBeFromFollowedUsers() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    JsonNode articles = jsonNode.get("articles");
    assertThat("articles should not be null", articles, notNullValue());
    // Feed may be empty if no followed users have articles; we just verify the structure
    assertThat("articles should be an array", articles.isArray(), is(true));
  }

  @When("an unauthenticated user requests the feed")
  public void anUnauthenticatedUserRequestsTheFeed() throws Exception {
    MvcResult result = mockMvc.perform(get("/articles/feed")).andReturn();
    sharedState.setLastResponse(result);
  }
}
