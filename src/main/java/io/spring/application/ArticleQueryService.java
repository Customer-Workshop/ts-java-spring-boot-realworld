package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.ArticleBookmarkDate;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.BookmarkedArticleData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleBookmarksReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleBookmarksReadService articleBookmarksReadService;

  public Optional<ArticleData> findById(String id, User user) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(id, user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public Optional<ArticleData> findBySlug(String slug, User user) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    } else {
      if (user != null) {
        fillExtraInfo(articleData.getId(), user, articleData);
      }
      return Optional.of(articleData);
    }
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      User currentUser) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      boolean hasExtra = articleIds.size() > page.getLimit();
      if (hasExtra) {
        articleIds.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articleIds);
      }

      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);

      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public CursorPager<ArticleData> findUserFeedWithCursor(
      User user, CursorPageParameter<DateTime> page) {
    List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followdUsers.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    } else {
      List<ArticleData> articles =
          articleReadService.findArticlesOfAuthorsWithCursor(followdUsers, page);
      boolean hasExtra = articles.size() > page.getLimit();
      if (hasExtra) {
        articles.remove(page.getLimit());
      }
      if (!page.isNext()) {
        Collections.reverse(articles);
      }
      fillExtraInfo(articles, user);
      return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }
  }

  public CursorPager<BookmarkedArticleData> findUserBookmarksWithCursor(
      User currentUser, CursorPageParameter<DateTime> page) {
    List<String> ids =
        articleBookmarksReadService.findUserBookmarkedArticleIdsWithCursor(
            currentUser.getId(), toTextCursor(page));
    if (ids.size() == 0) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    boolean hasExtra = ids.size() > page.getLimit();
    if (hasExtra) {
      ids.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(ids);
    }

    List<ArticleData> articles = findArticlesInIdOrder(ids);
    fillExtraInfo(articles, currentUser);

    Map<String, DateTime> bookmarkedAt = bookmarkDates(currentUser.getId(), ids);
    List<BookmarkedArticleData> data =
        articles.stream()
            .map(article -> new BookmarkedArticleData(article, bookmarkedAt.get(article.getId())))
            .collect(toList());
    return new CursorPager<>(data, page.getDirection(), hasExtra);
  }

  public ArticleDataList findUserBookmarks(User currentUser, Page page) {
    List<String> ids =
        articleBookmarksReadService.findUserBookmarkedArticleIds(currentUser.getId(), page);
    int count = articleBookmarksReadService.countUserBookmarks(currentUser.getId());
    if (ids.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), count);
    }
    List<ArticleData> articles = findArticlesInIdOrder(ids);
    fillExtraInfo(articles, currentUser);
    return new ArticleDataList(articles, count);
  }

  private List<ArticleData> findArticlesInIdOrder(List<String> ids) {
    Map<String, ArticleData> byId =
        articleReadService.findArticles(ids).stream()
            .collect(Collectors.toMap(ArticleData::getId, article -> article));
    return ids.stream().map(byId::get).filter(Objects::nonNull).collect(toList());
  }

  /**
   * {@code article_bookmarks.created_at} is persisted as a SQLite text timestamp (the column's
   * {@code CURRENT_TIMESTAMP} default), whereas a {@link DateTime} cursor binds as epoch millis.
   * Convert the cursor to the same text form so the bookmark-time comparison (D1) is correct; the
   * opaque cursor exposed to clients remains the {@link DateTimeCursor} (millis).
   */
  private CursorPageParameter<String> toTextCursor(CursorPageParameter<DateTime> page) {
    String cursor =
        page.getCursor() == null
            ? null
            : page.getCursor().withZone(DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss");
    return new CursorPageParameter<>(cursor, page.getLimit(), page.getDirection());
  }

  private Map<String, DateTime> bookmarkDates(String userId, List<String> ids) {
    return articleBookmarksReadService.findBookmarkDates(userId, ids).stream()
        .collect(
            Collectors.toMap(ArticleBookmarkDate::getArticleId, ArticleBookmarkDate::getCreatedAt));
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
    if (articleIds.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), articleCount);
    } else {
      List<ArticleData> articles = articleReadService.findArticles(articleIds);
      fillExtraInfo(articles, currentUser);
      return new ArticleDataList(articles, articleCount);
    }
  }

  public ArticleDataList findUserFeed(User user, Page page) {
    List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followdUsers.size() == 0) {
      return new ArticleDataList(new ArrayList<>(), 0);
    } else {
      List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
      fillExtraInfo(articles, user);
      int count = articleReadService.countFeedSize(followdUsers);
      return new ArticleDataList(articles, count);
    }
  }

  private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
    setFavoriteCount(articles);
    if (currentUser != null) {
      setIsFavorite(articles, currentUser);
      setIsFollowingAuthor(articles, currentUser);
    }
  }

  private void setIsFollowingAuthor(List<ArticleData> articles, User currentUser) {
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(
            currentUser.getId(),
            articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
                .collect(toList()));
    articles.forEach(
        articleData -> {
          if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
          }
        });
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(
        item -> {
          countMap.put(item.getId(), item.getCount());
        });
    articles.forEach(
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }

  private void setIsFavorite(List<ArticleData> articles, User currentUser) {
    Set<String> favoritedArticles =
        articleFavoritesReadService.userFavorites(
            articles.stream().map(articleData -> articleData.getId()).collect(toList()),
            currentUser);

    articles.forEach(
        articleData -> {
          if (favoritedArticles.contains(articleData.getId())) {
            articleData.setFavorited(true);
          }
        });
  }

  private void fillExtraInfo(String id, User user, ArticleData articleData) {
    articleData.setFavorited(articleFavoritesReadService.isUserFavorite(user.getId(), id));
    articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
    articleData
        .getProfileData()
        .setFollowing(
            userRelationshipQueryService.isUserFollowing(
                user.getId(), articleData.getProfileData().getId()));
  }
}
