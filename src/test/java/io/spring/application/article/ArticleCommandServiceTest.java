package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article() {
    NewArticleParam param =
        new NewArticleParam(
            "Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"));
    User creator = new User("test@example.com", "testuser", "password", "", "");
    Article savedArticle =
        new Article(
            "Test Title",
            "Test Description",
            "Test Body",
            Arrays.asList("java", "spring"),
            creator.getId());
    when(articleRepository.save(any(Article.class))).thenReturn(Mono.just(savedArticle));

    Article result = articleCommandService.createArticle(param, creator).block();

    assertThat(result, notNullValue());
    assertThat(result.getTitle(), is("Test Title"));
  }

  @Test
  public void should_update_article() {
    Article existingArticle =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-1");
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Description");
    when(articleRepository.save(any(Article.class))).thenReturn(Mono.just(existingArticle));

    Article result = articleCommandService.updateArticle(existingArticle, param).block();

    assertThat(result, notNullValue());
  }

  @Test
  public void should_create_article_with_empty_tags() {
    NewArticleParam param =
        new NewArticleParam("Test Title", "Test Description", "Test Body", Arrays.asList());
    User creator = new User("test@example.com", "testuser", "password", "", "");
    Article savedArticle =
        new Article(
            "Test Title", "Test Description", "Test Body", Arrays.asList(), creator.getId());
    when(articleRepository.save(any(Article.class))).thenReturn(Mono.just(savedArticle));

    Article result = articleCommandService.createArticle(param, creator).block();

    assertThat(result, notNullValue());
  }
}
