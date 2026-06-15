package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("A description")
            .body("Article body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-title", result.getSlug());
    assertEquals("Test Title", result.getTitle());
    assertEquals("A description", result.getDescription());
    assertEquals("Article body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(result);
  }

  @Test
  void should_update_article() {
    Article article = new Article("Original Title", "desc", "body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("new-title", result.getSlug());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_partial_params() {
    Article article = new Article("Original Title", "desc", "body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("body", result.getBody());
    assertEquals("desc", result.getDescription());
    verify(articleRepository).save(article);
  }
}
