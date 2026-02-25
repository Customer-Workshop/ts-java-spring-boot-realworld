package io.spring.cucumber.steps;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class FavoriteStepDefinitions {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private SharedState sharedState;

  public FavoriteStepDefinitions(SharedState sharedState) {
    this.sharedState = sharedState;
  }

  @When("the user favorites the article with slug {string}")
  public void theUserFavoritesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/articles/" + slug + "/favorite")
                    .header("Authorization", "Token " + sharedState.getCurrentToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @When("the user unfavorites the article with slug {string}")
  public void theUserUnfavoritesTheArticleWithSlug(String slug) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                delete("/articles/" + slug + "/favorite")
                    .header("Authorization", "Token " + sharedState.getCurrentToken()))
            .andReturn();

    sharedState.setLastResponse(result);
  }

  @Given("the article {string} already has {int} favorites")
  public void theArticleAlreadyHasFavorites(String slug, int count) {
    Article article = articleRepository.findBySlug(slug).orElseThrow();
    for (int i = 0; i < count; i++) {
      String fakeUsername = "fakeuser_fav_" + i;
      User fakeUser;
      if (userRepository.findByUsername(fakeUsername).isPresent()) {
        fakeUser = userRepository.findByUsername(fakeUsername).get();
      } else {
        fakeUser =
            new User(
                fakeUsername + "@example.com",
                fakeUsername,
                passwordEncoder.encode("password123"),
                "",
                "");
        userRepository.save(fakeUser);
      }
      ArticleFavorite favorite = new ArticleFavorite(article.getId(), fakeUser.getId());
      articleFavoriteRepository.save(favorite);
    }
  }

  @Given("the user has already favorited article {string}")
  public void theUserHasAlreadyFavoritedArticle(String slug) {
    Article article = articleRepository.findBySlug(slug).orElseThrow();
    ArticleFavorite favorite =
        new ArticleFavorite(article.getId(), sharedState.getCurrentUser().getId());
    articleFavoriteRepository.save(favorite);
  }
}
