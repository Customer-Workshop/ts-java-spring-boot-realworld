package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.time.LocalDateTime;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcCommentRepository implements CommentRepository {
  private final DatabaseClient databaseClient;

  public R2dbcCommentRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<Comment> save(Comment comment) {
    return databaseClient
        .sql(
            "INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) "
                + "VALUES (:id, :body, :articleId, :userId, :createdAt, :updatedAt)")
        .bind("id", comment.getId())
        .bind("body", comment.getBody())
        .bind("articleId", comment.getArticleId())
        .bind("userId", comment.getUserId())
        .bind("createdAt", comment.getCreatedAt())
        .bind("updatedAt", comment.getCreatedAt())
        .fetch()
        .rowsUpdated()
        .thenReturn(comment);
  }

  @Override
  public Mono<Comment> findById(String articleId, String id) {
    return databaseClient
        .sql("SELECT * FROM comments WHERE id = :id AND article_id = :articleId")
        .bind("id", id)
        .bind("articleId", articleId)
        .map(row -> mapRowToComment(row))
        .one();
  }

  @Override
  public Flux<Comment> findByArticleId(String articleId) {
    return databaseClient
        .sql("SELECT * FROM comments WHERE article_id = :articleId ORDER BY created_at DESC")
        .bind("articleId", articleId)
        .map(row -> mapRowToComment(row))
        .all();
  }

  private Comment mapRowToComment(io.r2dbc.spi.Readable row) {
    try {
      Comment comment = new Comment();
      var idField = Comment.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(comment, row.get("id", String.class));

      var bodyField = Comment.class.getDeclaredField("body");
      bodyField.setAccessible(true);
      bodyField.set(comment, row.get("body", String.class));

      var articleIdField = Comment.class.getDeclaredField("articleId");
      articleIdField.setAccessible(true);
      articleIdField.set(comment, row.get("article_id", String.class));

      var userIdField = Comment.class.getDeclaredField("userId");
      userIdField.setAccessible(true);
      userIdField.set(comment, row.get("user_id", String.class));

      var createdAtField = Comment.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(comment, row.get("created_at", LocalDateTime.class));

      return comment;
    } catch (Exception e) {
      throw new RuntimeException("Failed to map comment", e);
    }
  }

  @Override
  public Mono<Void> remove(Comment comment) {
    return databaseClient
        .sql("DELETE FROM comments WHERE id = :id")
        .bind("id", comment.getId())
        .fetch()
        .rowsUpdated()
        .then();
  }
}
