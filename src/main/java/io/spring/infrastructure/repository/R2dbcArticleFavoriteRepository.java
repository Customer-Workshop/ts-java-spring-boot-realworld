package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final DatabaseClient databaseClient;

  public R2dbcArticleFavoriteRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<ArticleFavorite> save(ArticleFavorite articleFavorite) {
    return find(articleFavorite.getArticleId(), articleFavorite.getUserId())
        .switchIfEmpty(
            databaseClient
                .sql(
                    "INSERT INTO article_favorites (article_id, user_id) VALUES (:articleId, :userId)")
                .bind("articleId", articleFavorite.getArticleId())
                .bind("userId", articleFavorite.getUserId())
                .fetch()
                .rowsUpdated()
                .thenReturn(articleFavorite));
  }

  @Override
  public Mono<ArticleFavorite> find(String articleId, String userId) {
    return databaseClient
        .sql("SELECT * FROM article_favorites WHERE article_id = :articleId AND user_id = :userId")
        .bind("articleId", articleId)
        .bind("userId", userId)
        .map(
            row ->
                new ArticleFavorite(
                    row.get("article_id", String.class), row.get("user_id", String.class)))
        .one();
  }

  @Override
  public Mono<Void> remove(ArticleFavorite favorite) {
    return databaseClient
        .sql("DELETE FROM article_favorites WHERE article_id = :articleId AND user_id = :userId")
        .bind("articleId", favorite.getArticleId())
        .bind("userId", favorite.getUserId())
        .fetch()
        .rowsUpdated()
        .then();
  }

  @Override
  public Mono<Long> countByArticleId(String articleId) {
    return databaseClient
        .sql("SELECT COUNT(*) as cnt FROM article_favorites WHERE article_id = :articleId")
        .bind("articleId", articleId)
        .map(row -> row.get("cnt", Long.class))
        .one()
        .defaultIfEmpty(0L);
  }
}
