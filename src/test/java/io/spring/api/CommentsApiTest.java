package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
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

public class CommentsApiTest {

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentQueryService commentQueryService;

  private CommentsApi commentsApi;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    commentsApi = new CommentsApi(articleRepository, commentRepository, commentQueryService);
  }

  @Test
  public void should_get_comments() {
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), "user-1");
    LocalDateTime now = LocalDateTime.now();
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    CommentData comment = new CommentData("comment-1", "Test comment", article.getId(), now, now, profile);
    
    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(commentQueryService.findByArticleId(anyString(), any())).thenReturn(Arrays.asList(comment));

    User user = new User("test@example.com", "testuser", "password", "", "");
    ResponseEntity response = commentsApi.getComments("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
  }

  @Test
  public void should_return_empty_comments() {
    Article article = new Article("Test Title", "Description", "Body", Arrays.asList("java"), "user-1");
    
    when(articleRepository.findBySlug("test-slug")).thenReturn(Mono.just(article));
    when(commentQueryService.findByArticleId(anyString(), any())).thenReturn(Collections.emptyList());

    User user = new User("test@example.com", "testuser", "password", "", "");
    ResponseEntity response = commentsApi.getComments("test-slug", user);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  public void should_throw_not_found_when_article_not_exists() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Mono.empty());

    User user = new User("test@example.com", "testuser", "password", "", "");

    assertThrows(ResourceNotFoundException.class, () -> {
      commentsApi.getComments("nonexistent", user);
    });
  }
}
