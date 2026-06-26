package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest(
    classes = {
      DgsAutoConfiguration.class,
      ArticleMutation.class,
      ArticleDatafetcher.class,
      GraphQLCustomizeExceptionHandler.class
    })
public class BookmarkMutationTest {

  @Autowired private DgsQueryExecutor dgsQueryExecutor;

  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private ArticleBookmarkRepository articleBookmarkRepository;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private UserRepository userRepository;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "", "");
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            false,
            0,
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
            new ProfileData(
                anotherUser.getId(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage(),
                false));
    when(articleQueryService.findById(eq(article.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(u, null, Collections.emptyList()));
  }

  private void anonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_bookmark_article_success() {
    authenticate(user);
    String slug =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { bookmarkArticle(slug: \""
                + article.getSlug()
                + "\") { article { slug } } }",
            "data.bookmarkArticle.article.slug");

    org.junit.jupiter.api.Assertions.assertEquals(article.getSlug(), slug);
    verify(articleBookmarkRepository).save(any());
  }

  @Test
  public void should_unbookmark_article_success() {
    authenticate(user);
    when(articleBookmarkRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(new ArticleBookmark(article.getId(), user.getId())));

    String slug =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { unbookmarkArticle(slug: \""
                + article.getSlug()
                + "\") { article { slug } } }",
            "data.unbookmarkArticle.article.slug");

    org.junit.jupiter.api.Assertions.assertEquals(article.getSlug(), slug);
    verify(articleBookmarkRepository).remove(new ArticleBookmark(article.getId(), user.getId()));
  }

  @Test
  public void should_error_when_anonymous_bookmark() {
    anonymous();
    org.junit.jupiter.api.Assertions.assertFalse(
        dgsQueryExecutor
            .execute(
                "mutation { bookmarkArticle(slug: \""
                    + article.getSlug()
                    + "\") { article { slug } } }")
            .getErrors()
            .isEmpty());
    verify(articleBookmarkRepository, never()).save(any());
  }

  @Test
  public void should_error_when_anonymous_unbookmark() {
    anonymous();
    org.junit.jupiter.api.Assertions.assertFalse(
        dgsQueryExecutor
            .execute(
                "mutation { unbookmarkArticle(slug: \""
                    + article.getSlug()
                    + "\") { article { slug } } }")
            .getErrors()
            .isEmpty());
    verify(articleBookmarkRepository, never()).remove(any());
  }

  @Test
  public void should_error_when_bookmark_unknown_slug() {
    authenticate(user);
    when(articleRepository.findBySlug(eq("unknown"))).thenReturn(Optional.empty());
    org.junit.jupiter.api.Assertions.assertFalse(
        dgsQueryExecutor
            .execute("mutation { bookmarkArticle(slug: \"unknown\") { article { slug } } }")
            .getErrors()
            .isEmpty());
  }

  @Test
  public void should_be_noop_when_unbookmark_not_bookmarked() {
    authenticate(user);
    when(articleBookmarkRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    String slug =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { unbookmarkArticle(slug: \""
                + article.getSlug()
                + "\") { article { slug } } }",
            "data.unbookmarkArticle.article.slug");

    org.junit.jupiter.api.Assertions.assertEquals(article.getSlug(), slug);
    verify(articleBookmarkRepository, never()).remove(any());
  }

  @Test
  public void should_not_change_favorites_metadata_on_bookmark() {
    authenticate(user);
    Boolean favorited =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { bookmarkArticle(slug: \""
                + article.getSlug()
                + "\") { article { favorited favoritesCount } } }",
            "data.bookmarkArticle.article.favorited");
    Integer favoritesCount =
        dgsQueryExecutor.executeAndExtractJsonPath(
            "mutation { bookmarkArticle(slug: \""
                + article.getSlug()
                + "\") { article { favorited favoritesCount } } }",
            "data.bookmarkArticle.article.favoritesCount");

    org.junit.jupiter.api.Assertions.assertEquals(false, favorited);
    org.junit.jupiter.api.Assertions.assertEquals(0, favoritesCount);
    verify(articleBookmarkRepository, times(2)).save(any());
  }
}
