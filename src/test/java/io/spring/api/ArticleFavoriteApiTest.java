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
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
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

public class ArticleFavoriteApiTest {

  @Mock private ArticleFavoriteRepository articleFavoriteRepository;

  @Mock private ArticleRepository articleRepository;

  @Mock private ArticleQueryService articleQueryService;

  private ArticleFavoriteApi articleFavoriteApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleFavoriteApi =
        new ArticleFavoriteApi(articleFavoriteRepository, articleRepository, articleQueryService);
  }

  @Test
  public void should_favorite_article() {
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java"), "user-1");
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            "test-slug",
            "Test Title",
            "Description",
            "Body",
            true,
            1,
            now,
            now,
            Arrays.asList("java"),
            profile);

    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(articleFavoriteRepository.save(any(ArticleFavorite.class))).thenReturn(Mono.empty());
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.of(articleData));

    User user = new User("test@example.com", "testuser", "password", "", "");
    ResponseEntity response = articleFavoriteApi.favoriteArticle("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_throw_not_found_when_favorite_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Mono.empty());

    User user = new User("test@example.com", "testuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          articleFavoriteApi.favoriteArticle("nonexistent", user);
        });
  }

  @Test
  public void should_unfavorite_article() {
    Article article =
        new Article("Test Title", "Description", "Body", Arrays.asList("java"), "user-1");
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), "user-2");
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ArticleData articleData =
        new ArticleData(
            article.getId(),
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

    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(articleFavoriteRepository.find(anyString(), anyString())).thenReturn(Mono.just(favorite));
    when(articleFavoriteRepository.remove(any(ArticleFavorite.class))).thenReturn(Mono.empty());
    when(articleQueryService.findBySlug(anyString(), any())).thenReturn(Optional.of(articleData));

    User user = new User("test@example.com", "testuser", "password", "", "");
    ResponseEntity response = articleFavoriteApi.unfavoriteArticle("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_throw_not_found_when_unfavorite_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Mono.empty());

    User user = new User("test@example.com", "testuser", "password", "", "");

    assertThrows(
        ResourceNotFoundException.class,
        () -> {
          articleFavoriteApi.unfavoriteArticle("nonexistent", user);
        });
  }
}
