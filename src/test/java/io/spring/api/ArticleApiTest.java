package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class ArticleApiTest {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private ArticleRepository articleRepository;

  @Mock private ArticleCommandService articleCommandService;

  private ArticleApi articleApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleApi = new ArticleApi(articleQueryService, articleRepository, articleCommandService);
  }

  @Test
  public void should_get_article() {
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.of(articleData));

    User user = new User("test@example.com", "testuser", "password", "", "");
    ResponseEntity response = articleApi.article("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_throw_not_found_when_article_not_exists() {
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.empty());

    User user = new User("test@example.com", "testuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          articleApi.article("nonexistent", user);
        });
  }

  @Test
  public void should_delete_article() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(articleRepository.remove(any(Article.class))).thenReturn(Mono.empty());

    ResponseEntity response = articleApi.deleteArticle("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
  }

  @Test
  public void should_throw_not_found_when_delete_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Mono.empty());

    User user = new User("test@example.com", "testuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          articleApi.deleteArticle("nonexistent", user);
        });
  }

  @Test
  public void should_update_article() {
    User user = new User("test@example.com", "testuser", "password", "", "");
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java"), user.getId());
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            "article-1",
            "new-slug",
            "New Title",
            "New Description",
            "New Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    UpdateArticleParam updateParam =
        new UpdateArticleParam("New Title", "New Body", "New Description");

    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(articleCommandService.updateArticle(any(Article.class), any(UpdateArticleParam.class)))
        .thenReturn(Mono.just(article));
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.of(articleData));

    ResponseEntity response = articleApi.updateArticle("test-slug", user, updateParam);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }
}
