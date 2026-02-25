package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  void should_create_article() {
    User creator = new User("a@b.com", "user1", "pass", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test article")
            .body("Article body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Test Article", article.getTitle());
    assertEquals("A test article", article.getDescription());
    assertEquals("Article body content", article.getBody());
    assertEquals(creator.getId(), article.getUserId());
    assertNotNull(article.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    User creator = new User("a@b.com", "user1", "pass", "", "");
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("tag"), creator.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", updated.getTitle());
    assertEquals("new body", updated.getBody());
    assertEquals("new desc", updated.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_partial_params() {
    User creator = new User("a@b.com", "user1", "pass", "", "");
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("tag"), creator.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", updated.getTitle());
    verify(articleRepository).save(article);
  }
}
