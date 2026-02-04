package io.spring.application.facade;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleApiFacade {

  private final ArticleCommandService articleCommandService;
  private final ArticleQueryService articleQueryService;
  private final ArticleRepository articleRepository;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  public ArticleData createArticle(NewArticleParam newArticleParam, User user) {
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return articleQueryService
        .findById(article.getId(), user)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public ArticleData getArticle(String slug, User user) {
    return articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new);
  }

  public ArticleData updateArticle(String slug, UpdateArticleParam updateArticleParam, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }
    Article updatedArticle = articleCommandService.updateArticle(article, updateArticleParam);
    return articleQueryService
        .findBySlug(updatedArticle.getSlug(), user)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public void deleteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }
    articleRepository.remove(article);
  }

  public ArticleDataList getArticles(
      String tag, String author, String favoritedBy, Page page, User user) {
    return articleQueryService.findRecentArticles(tag, author, favoritedBy, page, user);
  }

  public ArticleDataList getUserFeed(User user, Page page) {
    return articleQueryService.findUserFeed(user, page);
  }

  public CursorPager<ArticleData> getArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      User user) {
    return articleQueryService.findRecentArticlesWithCursor(tag, author, favoritedBy, page, user);
  }

  public CursorPager<ArticleData> getUserFeedWithCursor(
      User user, CursorPageParameter<DateTime> page) {
    return articleQueryService.findUserFeedWithCursor(user, page);
  }

  public ArticleData favoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(articleFavorite);
    return articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new);
  }

  public ArticleData unfavoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));
    return articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new);
  }
}
