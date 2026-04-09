package io.spring.application.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  private User user;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    Assertions.assertNotNull(article);
    Assertions.assertEquals("Test Title", article.getTitle());
    Assertions.assertEquals("Test Description", article.getDescription());
    Assertions.assertEquals("Test Body", article.getBody());
    Assertions.assertEquals(user.getId(), article.getUserId());
    Assertions.assertEquals("test-title", article.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("Description")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, user);

    Assertions.assertNotNull(article);
    Assertions.assertTrue(article.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_null_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Null Tags Article")
            .description("Description")
            .body("Body")
            .tagList(null)
            .build();

    Assertions.assertThrows(
        NullPointerException.class, () -> articleCommandService.createArticle(param, user));
  }

  @Test
  public void should_update_article_title() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("New Title", updated.getTitle());
    Assertions.assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_body_and_description() {
    Article article =
        new Article("Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("", "New Body", "New Desc");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("New Body", updated.getBody());
    Assertions.assertEquals("New Desc", updated.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_all_fields() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam =
        new UpdateArticleParam("New Title", "New Body", "New Description");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("New Title", updated.getTitle());
    Assertions.assertEquals("New Body", updated.getBody());
    Assertions.assertEquals("New Description", updated.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_preserve_article_id_after_update() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    String originalId = article.getId();

    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", "", "");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals(originalId, updated.getId());
  }
}
