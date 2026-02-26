package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class TagStepDefinitions {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper responseMapper =
      new ObjectMapper().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private SharedState sharedState;

  public TagStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @Given("the following tags exist in the system:")
  public void theFollowingTagsExistInTheSystem(DataTable dataTable) {
    List<String> tags = dataTable.asList(String.class);

    User author;
    Optional<User> existingAuthor = userRepository.findByUsername("tagauthor");
    if (existingAuthor.isPresent()) {
      author = existingAuthor.get();
    } else {
      author =
          new User(
              "tagauthor@example.com", "tagauthor", passwordEncoder.encode("password123"), "", "");
      userRepository.save(author);
    }

    // Create an article with each tag to ensure tags exist
    for (String tag : tags) {
      String title = "Tag Article " + tag + "-" + System.nanoTime();
      Article article =
          new Article(
              title,
              "Description for " + tag,
              "Body for " + tag,
              Collections.singletonList(tag),
              author.getId());
      articleRepository.save(article);
    }
  }

  @Given("an article exists with tags {string}")
  public void anArticleExistsWithTags(String tagsStr) {
    List<String> tags = Arrays.asList(tagsStr.split(","));

    User author;
    Optional<User> existingAuthor = userRepository.findByUsername("tagauthor2");
    if (existingAuthor.isPresent()) {
      author = existingAuthor.get();
    } else {
      author =
          new User(
              "tagauthor2@example.com",
              "tagauthor2",
              passwordEncoder.encode("password123"),
              "",
              "");
      userRepository.save(author);
    }

    Article article =
        new Article(
            "Tagged Article " + System.nanoTime(),
            "Description",
            "Body",
            tags,
            author.getId());
    articleRepository.save(article);
  }

  @When("the user requests the tags list")
  public void theUserRequestsTheTagsList() throws Exception {
    MvcResult result;
    if (sharedState.getCurrentToken() != null) {
      result =
          mockMvc
              .perform(
                  get("/tags").header("Authorization", "Token " + sharedState.getCurrentToken()))
              .andReturn();
    } else {
      result = mockMvc.perform(get("/tags")).andReturn();
    }
    sharedState.setLastResponse(result);
  }

  @When("an unauthenticated user requests the tags list")
  public void anUnauthenticatedUserRequestsTheTagsList() throws Exception {
    MvcResult result = mockMvc.perform(get("/tags")).andReturn();
    sharedState.setLastResponse(result);
  }

  @Then("the tags list should contain {string}")
  public void theTagsListShouldContain(String tag) throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode tags = jsonNode.get("tags");
    assertThat("tags should not be null", tags, notNullValue());
    boolean found = false;
    for (JsonNode tagNode : tags) {
      if (tagNode.asText().equals(tag)) {
        found = true;
        break;
      }
    }
    assertThat("Tags should contain '" + tag + "'", found, is(true));
  }

  @Then("the tags list should not be empty")
  public void theTagsListShouldNotBeEmpty() throws Exception {
    String responseBody = sharedState.getLastResponse().getResponse().getContentAsString();
    JsonNode jsonNode = responseMapper.readTree(responseBody);
    JsonNode tags = jsonNode.get("tags");
    assertThat("tags should not be null", tags, notNullValue());
    assertThat("tags list should not be empty", tags.size(), greaterThan(0));
  }
}
