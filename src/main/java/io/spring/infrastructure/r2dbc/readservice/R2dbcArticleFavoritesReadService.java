package io.spring.infrastructure.r2dbc.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcArticleFavoritesReadService implements ArticleFavoritesReadService {

  private final DatabaseClient db;

  public R2dbcArticleFavoritesReadService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    Long count =
        db.sql(
                "SELECT COUNT(*) as cnt FROM article_favorites WHERE user_id = :userId AND article_id = :articleId")
            .bind("userId", userId)
            .bind("articleId", articleId)
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .block();
    return count != null && count > 0;
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    Long count =
        db.sql("SELECT COUNT(*) as cnt FROM article_favorites WHERE article_id = :articleId")
            .bind("articleId", articleId)
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .block();
    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    String placeholders = String.join(",", ids.stream().map(id -> "'" + id + "'").toList());
    return db.sql(
            "SELECT article_id, COUNT(*) as cnt FROM article_favorites WHERE article_id IN ("
                + placeholders
                + ") GROUP BY article_id")
        .map(
            (row, metadata) ->
                new ArticleFavoriteCount(
                    row.get("article_id", String.class), row.get("cnt", Long.class).intValue()))
        .all()
        .collectList()
        .block();
  }

  @Override
  public Set<String> userFavorites(List<String> ids, User currentUser) {
    if (ids == null || ids.isEmpty() || currentUser == null) {
      return new HashSet<>();
    }
    String placeholders = String.join(",", ids.stream().map(id -> "'" + id + "'").toList());
    List<String> favorites =
        db.sql(
                "SELECT article_id FROM article_favorites WHERE user_id = :userId AND article_id IN ("
                    + placeholders
                    + ")")
            .bind("userId", currentUser.getId())
            .map((row, metadata) -> row.get("article_id", String.class))
            .all()
            .collectList()
            .block();
    return favorites != null ? new HashSet<>(favorites) : new HashSet<>();
  }
}
