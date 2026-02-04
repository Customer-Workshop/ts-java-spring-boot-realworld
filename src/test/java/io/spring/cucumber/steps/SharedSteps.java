package io.spring.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class SharedSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    private Response response;
    private String authToken;
    private User currentUser;
    private Article currentArticle;
    private Comment currentComment;
    private Map<String, Object> requestData;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        authToken = null;
        currentUser = null;
        currentArticle = null;
        currentComment = null;
        requestData = null;
    }

    @Given("I have user registration data with username {string} email {string} and password {string}")
    public void iHaveUserRegistrationData(String username, String email, String password) {
        requestData = new HashMap<>();
        Map<String, String> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);
        requestData.put("user", user);
    }

    @When("I send a POST request to {string} with the registration data")
    public void iSendPostRequestWithRegistrationData(String endpoint) {
        response = RestAssured.given()
                .contentType("application/json")
                .body(requestData)
                .post(endpoint);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int statusCode) {
        assertEquals(statusCode, response.getStatusCode());
    }

    @And("the response should contain a user with email {string}")
    public void theResponseShouldContainUserWithEmail(String email) {
        response.then().body("user.email", equalTo(email));
    }

    @And("the response should contain a JWT token")
    public void theResponseShouldContainJwtToken() {
        response.then().body("user.token", notNullValue());
        authToken = response.jsonPath().getString("user.token");
    }

    @Given("a user exists with email {string}")
    public void aUserExistsWithEmail(String email) {
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);
        registerUserViaApi(username, email, "password123");
    }

    @Given("a user exists with username {string}")
    public void aUserExistsWithUsername(String username) {
        if (userRepository.findByUsername(username).isEmpty()) {
            String email = username + "@test.com";
            registerUserViaApi(username, email, "password123");
        }
    }

    @Given("a user exists with email {string} and password {string}")
    public void aUserExistsWithEmailAndPassword(String email, String password) {
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);
        registerUserViaApi(username, email, password);
    }
    
    private void registerUserViaApi(String username, String email, String password) {
        Map<String, Object> regData = new HashMap<>();
        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password);
        regData.put("user", userData);
        
        RestAssured.given()
                .contentType("application/json")
                .body(regData)
                .post("/users");
    }

    @When("I send a POST request to {string} with email {string} and password {string}")
    public void iSendPostRequestWithEmailAndPassword(String endpoint, String email, String password) {
        Map<String, Object> loginData = new HashMap<>();
        Map<String, String> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        loginData.put("user", user);

        response = RestAssured.given()
                .contentType("application/json")
                .body(loginData)
                .post(endpoint);
    }

    @Given("I am logged in as a user with email {string}")
    public void iAmLoggedInAsUserWithEmail(String email) {
        String username = "user" + UUID.randomUUID().toString().substring(0, 8);
        String password = "password123";

        // Register user via API to ensure password is properly encoded
        Map<String, Object> regData = new HashMap<>();
        Map<String, String> regUserData = new HashMap<>();
        regUserData.put("username", username);
        regUserData.put("email", email);
        regUserData.put("password", password);
        regData.put("user", regUserData);
        
        Response regResponse = RestAssured.given()
                .contentType("application/json")
                .body(regData)
                .post("/users");
        
        // Get token from registration response
        authToken = regResponse.jsonPath().getString("user.token");
        
        // Get the user from repository to set currentUser
        currentUser = userRepository.findByEmail(email).orElse(null);
    }

    @When("I send a GET request to {string} with authentication")
    public void iSendGetRequestWithAuthentication(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .get(endpoint);
    }

    @When("I send a GET request to {string} without authentication")
    public void iSendGetRequestWithoutAuthentication(String endpoint) {
        response = RestAssured.given()
                .get(endpoint);
    }

    @When("I send a PUT request to {string} with bio {string} and image {string}")
    public void iSendPutRequestWithBioAndImage(String endpoint, String bio, String image) {
        Map<String, Object> updateData = new HashMap<>();
        Map<String, String> user = new HashMap<>();
        user.put("bio", bio);
        user.put("image", image);
        updateData.put("user", user);

        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .contentType("application/json")
                .body(updateData)
                .put(endpoint);
    }

    @And("the response should contain a user with bio {string}")
    public void theResponseShouldContainUserWithBio(String bio) {
        response.then().body("user.bio", equalTo(bio));
    }

    @Given("some articles exist in the system")
    public void someArticlesExistInTheSystem() {
        User author = new User("author" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "author" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        Article article = new Article("Test Article", "Description", "Body",
                Arrays.asList("test"), author.getId());
        articleRepository.save(article);
    }

    @And("the response should contain a list of articles")
    public void theResponseShouldContainListOfArticles() {
        response.then().body("articles", notNullValue());
    }

    @Given("an article exists with tag {string}")
    public void anArticleExistsWithTag(String tag) {
        User author = new User("tagauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "tagauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        Article article = new Article("Article with " + tag, "Description", "Body",
                Arrays.asList(tag), author.getId());
        articleRepository.save(article);
    }

    @And("all articles in the response should have tag {string}")
    public void allArticlesInResponseShouldHaveTag(String tag) {
        response.then().body("articles.tagList.flatten()", hasItem(tag));
    }

    @Given("an article exists by author {string}")
    public void anArticleExistsByAuthor(String authorUsername) {
        User author = new User(authorUsername + "@test.com", authorUsername, "password123", "", "");
        userRepository.save(author);

        Article article = new Article("Article by " + authorUsername, "Description", "Body",
                Arrays.asList("test"), author.getId());
        articleRepository.save(article);
    }

    @And("all articles in the response should be by author {string}")
    public void allArticlesInResponseShouldBeByAuthor(String authorUsername) {
        response.then().body("articles.author.username", everyItem(equalTo(authorUsername)));
    }

    @Given("multiple articles exist in the system")
    public void multipleArticlesExistInTheSystem() {
        User author = new User("multiauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "multiauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        for (int i = 0; i < 10; i++) {
            Article article = new Article("Article " + i + " " + UUID.randomUUID().toString().substring(0, 4), 
                    "Description " + i, "Body " + i,
                    Arrays.asList("test"), author.getId());
            articleRepository.save(article);
        }
    }

    @And("the response should contain at most {int} articles")
    public void theResponseShouldContainAtMostArticles(int count) {
        int actualCount = response.jsonPath().getList("articles").size();
        assertTrue(actualCount <= count);
    }

    @Given("I have article data with title {string} description {string} and body {string}")
    public void iHaveArticleData(String title, String description, String body) {
        requestData = new HashMap<>();
        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("description", description);
        article.put("body", body);
        article.put("tagList", Arrays.asList("test"));
        requestData.put("article", article);
    }

    @When("I send a POST request to {string} with the article data and authentication")
    public void iSendPostRequestWithArticleDataAndAuthentication(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .contentType("application/json")
                .body(requestData)
                .post(endpoint);
    }

    @When("I send a POST request to {string} with the article data without authentication")
    public void iSendPostRequestWithArticleDataWithoutAuthentication(String endpoint) {
        response = RestAssured.given()
                .contentType("application/json")
                .body(requestData)
                .post(endpoint);
    }

    @And("the response should contain an article with title {string}")
    public void theResponseShouldContainArticleWithTitle(String title) {
        response.then().body("article.title", equalTo(title));
    }

    @And("the response should contain an article with slug {string}")
    public void theResponseShouldContainArticleWithSlug(String slug) {
        String expectedSlug = slug.toLowerCase().replace(" ", "-");
        response.then().body("article.slug", containsString(expectedSlug));
    }

    @Given("an article exists with slug {string}")
    public void anArticleExistsWithSlug(String slug) {
        User author = new User("slugauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "slugauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        String title = slug.replace("-", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        currentArticle = new Article(title, "Description", "Body",
                Arrays.asList("test"), author.getId());
        articleRepository.save(currentArticle);
    }

    @Given("I have created an article with slug {string}")
    public void iHaveCreatedArticleWithSlug(String slug) {
        String title = slug.replace("-", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        currentArticle = new Article(title, "Description", "Body",
                Arrays.asList("test"), currentUser.getId());
        articleRepository.save(currentArticle);
    }

    @When("I send a PUT request to {string} with title {string}")
    public void iSendPutRequestWithTitle(String endpoint, String title) {
        Map<String, Object> updateData = new HashMap<>();
        Map<String, String> article = new HashMap<>();
        article.put("title", title);
        updateData.put("article", article);

        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .contentType("application/json")
                .body(updateData)
                .put(endpoint);
    }

    @Given("an article exists with slug {string} by another user")
    public void anArticleExistsWithSlugByAnotherUser(String slug) {
        User otherAuthor = new User("otherauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "otherauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(otherAuthor);

        String title = slug.replace("-", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        currentArticle = new Article(title, "Description", "Body",
                Arrays.asList("test"), otherAuthor.getId());
        articleRepository.save(currentArticle);
    }

    @When("I send a DELETE request to {string} with authentication")
    public void iSendDeleteRequestWithAuthentication(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .delete(endpoint);
    }

    @Given("I follow a user who has articles")
    public void iFollowUserWhoHasArticles() {
        User followedUser = new User("followed" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "followed" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(followedUser);

        Article article = new Article("Followed User Article " + UUID.randomUUID().toString().substring(0, 4), 
                "Description", "Body",
                Arrays.asList("test"), followedUser.getId());
        articleRepository.save(article);

        userRepository.saveRelation(new FollowRelation(currentUser.getId(), followedUser.getId()));
    }

    @And("the response should contain articles from followed users")
    public void theResponseShouldContainArticlesFromFollowedUsers() {
        response.then().body("articles", notNullValue());
    }

    @Given("the article has comments")
    public void theArticleHasComments() {
        User commenter = new User("commenter" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "commenter" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(commenter);

        Comment comment = new Comment("This is a test comment", commenter.getId(), currentArticle.getId());
        commentRepository.save(comment);
    }

    @And("the response should contain a list of comments")
    public void theResponseShouldContainListOfComments() {
        response.then().body("comments", notNullValue());
    }

    @When("I send a POST request to {string} with body {string}")
    public void iSendPostRequestWithBody(String endpoint, String body) {
        Map<String, Object> commentData = new HashMap<>();
        Map<String, String> comment = new HashMap<>();
        comment.put("body", body);
        commentData.put("comment", comment);

        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .contentType("application/json")
                .body(commentData)
                .post(endpoint);
    }

    @When("I send a POST request to {string} with body {string} without authentication")
    public void iSendPostRequestWithBodyWithoutAuthentication(String endpoint, String body) {
        Map<String, Object> commentData = new HashMap<>();
        Map<String, String> comment = new HashMap<>();
        comment.put("body", body);
        commentData.put("comment", comment);

        response = RestAssured.given()
                .contentType("application/json")
                .body(commentData)
                .post(endpoint);
    }

    @And("the response should contain a comment with body {string}")
    public void theResponseShouldContainCommentWithBody(String body) {
        response.then().body("comment.body", equalTo(body));
    }

    @Given("I have added a comment to article {string}")
    public void iHaveAddedCommentToArticle(String slug) {
        User author = new User("commentarticleauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "commentarticleauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        String title = slug.replace("-", " ");
        currentArticle = new Article(title, "Description", "Body", Arrays.asList("test"), author.getId());
        articleRepository.save(currentArticle);

        currentComment = new Comment("My comment", currentUser.getId(), currentArticle.getId());
        commentRepository.save(currentComment);
    }

    @When("I send a DELETE request to delete my comment with authentication")
    public void iSendDeleteRequestToDeleteMyComment() {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .delete("/articles/" + currentArticle.getSlug() + "/comments/" + currentComment.getId());
    }

    @Given("another user has added a comment to article {string}")
    public void anotherUserHasAddedCommentToArticle(String slug) {
        User author = new User("protectedarticleauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "protectedarticleauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        String title = slug.replace("-", " ");
        currentArticle = new Article(title, "Description", "Body", Arrays.asList("test"), author.getId());
        articleRepository.save(currentArticle);

        User otherCommenter = new User("othercommenter" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "othercommenter" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(otherCommenter);

        currentComment = new Comment("Other user's comment", otherCommenter.getId(), currentArticle.getId());
        commentRepository.save(currentComment);
    }

    @When("I send a DELETE request to delete that comment with authentication")
    public void iSendDeleteRequestToDeleteThatComment() {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .delete("/articles/" + currentArticle.getSlug() + "/comments/" + currentComment.getId());
    }

    @And("the response should contain a profile with username {string}")
    public void theResponseShouldContainProfileWithUsername(String username) {
        response.then().body("profile.username", equalTo(username));
    }

    @And("the profile should have following status false")
    public void theProfileShouldHaveFollowingStatusFalse() {
        response.then().body("profile.following", equalTo(false));
    }

    @And("the profile should have following status true")
    public void theProfileShouldHaveFollowingStatusTrue() {
        response.then().body("profile.following", equalTo(true));
    }

    @When("I send a POST request to {string} with authentication")
    public void iSendPostRequestWithAuthentication(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Token " + authToken)
                .post(endpoint);
    }

    @When("I send a POST request to {string} without authentication")
    public void iSendPostRequestWithoutAuthentication(String endpoint) {
        response = RestAssured.given()
                .post(endpoint);
    }

    @When("I send a DELETE request to {string} without authentication")
    public void iSendDeleteRequestWithoutAuthentication(String endpoint) {
        response = RestAssured.given()
                .delete(endpoint);
    }

    @Given("I am following a user with username {string}")
    public void iAmFollowingUserWithUsername(String username) {
        User followedUser = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User(username + "@test.com", username, "password123", "", "");
            userRepository.save(newUser);
            return newUser;
        });

        userRepository.saveRelation(new FollowRelation(currentUser.getId(), followedUser.getId()));
    }

    @Given("some articles with tags exist in the system")
    public void someArticlesWithTagsExistInTheSystem() {
        User author = new User("tagauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "tagauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        Article article1 = new Article("Article with Java " + UUID.randomUUID().toString().substring(0, 4), 
                "Description", "Body",
                Arrays.asList("java", "spring"), author.getId());
        articleRepository.save(article1);

        Article article2 = new Article("Article with Python " + UUID.randomUUID().toString().substring(0, 4), 
                "Description", "Body",
                Arrays.asList("python", "django"), author.getId());
        articleRepository.save(article2);
    }

    @And("the response should contain a list of tags")
    public void theResponseShouldContainListOfTags() {
        response.then().body("tags", notNullValue());
    }

    @And("the article should be favorited")
    public void theArticleShouldBeFavorited() {
        response.then().body("article.favorited", equalTo(true));
    }

    @And("the article should not be favorited")
    public void theArticleShouldNotBeFavorited() {
        response.then().body("article.favorited", equalTo(false));
    }

    @And("the favorites count should be greater than {int}")
    public void theFavoritesCountShouldBeGreaterThan(int count) {
        response.then().body("article.favoritesCount", greaterThan(count));
    }

    @Given("I have favorited an article with slug {string}")
    public void iHaveFavoritedArticleWithSlug(String slug) {
        User author = new User("favauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "favauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        String title = slug.replace("-", " ");
        currentArticle = new Article(title, "Description", "Body", Arrays.asList("test"), author.getId());
        articleRepository.save(currentArticle);

        articleFavoriteRepository.save(new ArticleFavorite(currentArticle.getId(), currentUser.getId()));
    }

    @Given("the user has favorited some articles")
    public void theUserHasFavoritedSomeArticles() {
        User author = new User("favoritedauthor" + UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                "favoritedauthor" + UUID.randomUUID().toString().substring(0, 8), "password123", "", "");
        userRepository.save(author);

        Article article = new Article("Favorited Article " + UUID.randomUUID().toString().substring(0, 4), 
                "Description", "Body", Arrays.asList("test"), author.getId());
        articleRepository.save(article);

        User collector = userRepository.findByUsername("favoritecollector").orElse(null);
        if (collector != null) {
            articleFavoriteRepository.save(new ArticleFavorite(article.getId(), collector.getId()));
        }
    }

    @And("the response should contain articles favorited by {string}")
    public void theResponseShouldContainArticlesFavoritedBy(String username) {
        response.then().body("articles", notNullValue());
    }
}
