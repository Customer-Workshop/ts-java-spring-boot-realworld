package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class ArticlesApiTest {

  @Mock private ArticleCommandService articleCommandService;

  @Mock private ArticleQueryService articleQueryService;

  private ArticlesApi articlesApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articlesApi = new ArticlesApi(articleCommandService, articleQueryService);
  }

  @Test
  public void should_create_article() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java"), user.getId());
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData(user.getId(), "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            "test-title",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    NewArticleParam newArticleParam =
        new NewArticleParam("Test Title", "Description", "Body", Arrays.asList("java"));

    when(articleCommandService.createArticle(any(NewArticleParam.class), any(User.class)))
        .thenReturn(Mono.just(article));
    when(articleQueryService.findById(anyString(), any(User.class)))
        .thenReturn(Optional.of(articleData));

    ResponseEntity response = articlesApi.createArticle(newArticleParam, user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_get_feed() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    ArticleDataList articleDataList = new ArticleDataList(Collections.emptyList(), 0);

    when(articleQueryService.findUserFeed(any(User.class), any(Page.class)))
        .thenReturn(articleDataList);

    ResponseEntity response = articlesApi.getFeed(0, 20, user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_get_articles() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    ArticleDataList articleDataList = new ArticleDataList(Collections.emptyList(), 0);

    when(articleQueryService.findRecentArticles(
            anyString(), anyString(), anyString(), any(Page.class), any(User.class)))
        .thenReturn(articleDataList);

    ResponseEntity response = articlesApi.getArticles(0, 20, "java", "testuser", "testuser", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_search_articles() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    ArticleDataList articleDataList = new ArticleDataList(Collections.emptyList(), 0);

    when(articleQueryService.searchArticles(anyString(), any(Page.class), any(User.class)))
        .thenReturn(articleDataList);

    ResponseEntity response = articlesApi.searchArticles("spring", 0, 20, user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }
}
