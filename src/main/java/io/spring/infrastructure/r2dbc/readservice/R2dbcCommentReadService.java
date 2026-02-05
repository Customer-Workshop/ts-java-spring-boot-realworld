package io.spring.infrastructure.r2dbc.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcCommentReadService implements CommentReadService {

  private final DatabaseClient db;

  public R2dbcCommentReadService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public CommentData findById(String id) {
    return db.sql(
            "SELECT c.id, c.body, c.article_id, c.created_at, c.updated_at, "
                + "u.id as user_id, u.username, u.bio, u.image "
                + "FROM comments c "
                + "JOIN users u ON c.user_id = u.id "
                + "WHERE c.id = :id")
        .bind("id", id)
        .map(
            (row, metadata) -> {
              ProfileData profile =
                  new ProfileData(
                      row.get("user_id", String.class),
                      row.get("username", String.class),
                      row.get("bio", String.class),
                      row.get("image", String.class),
                      false);
              return new CommentData(
                  row.get("id", String.class),
                  row.get("body", String.class),
                  row.get("article_id", String.class),
                  row.get("created_at", LocalDateTime.class),
                  row.get("updated_at", LocalDateTime.class),
                  profile);
            })
        .one()
        .block();
  }

  @Override
  public List<CommentData> findByArticleId(String articleId) {
    return db.sql(
            "SELECT c.id, c.body, c.article_id, c.created_at, c.updated_at, "
                + "u.id as user_id, u.username, u.bio, u.image "
                + "FROM comments c "
                + "JOIN users u ON c.user_id = u.id "
                + "WHERE c.article_id = :articleId "
                + "ORDER BY c.created_at DESC")
        .bind("articleId", articleId)
        .map(
            (row, metadata) -> {
              ProfileData profile =
                  new ProfileData(
                      row.get("user_id", String.class),
                      row.get("username", String.class),
                      row.get("bio", String.class),
                      row.get("image", String.class),
                      false);
              return new CommentData(
                  row.get("id", String.class),
                  row.get("body", String.class),
                  row.get("article_id", String.class),
                  row.get("created_at", LocalDateTime.class),
                  row.get("updated_at", LocalDateTime.class),
                  profile);
            })
        .all()
        .collectList()
        .block();
  }

  @Override
  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<LocalDateTime> page) {
    String direction = page.getDirection().name().equals("NEXT") ? "<" : ">";
    String order = page.getDirection().name().equals("NEXT") ? "DESC" : "ASC";
    LocalDateTime cursor = page.getCursor();

    String sql =
        "SELECT c.id, c.body, c.article_id, c.created_at, c.updated_at, "
            + "u.id as user_id, u.username, u.bio, u.image "
            + "FROM comments c "
            + "JOIN users u ON c.user_id = u.id "
            + "WHERE c.article_id = :articleId ";

    if (cursor != null) {
      sql += "AND c.created_at " + direction + " :cursor ";
    }
    sql += "ORDER BY c.created_at " + order + " LIMIT :limit";

    DatabaseClient.GenericExecuteSpec spec =
        db.sql(sql).bind("articleId", articleId).bind("limit", page.getLimit());

    if (cursor != null) {
      spec = spec.bind("cursor", cursor);
    }

    return spec.map(
            (row, metadata) -> {
              ProfileData profile =
                  new ProfileData(
                      row.get("user_id", String.class),
                      row.get("username", String.class),
                      row.get("bio", String.class),
                      row.get("image", String.class),
                      false);
              return new CommentData(
                  row.get("id", String.class),
                  row.get("body", String.class),
                  row.get("article_id", String.class),
                  row.get("created_at", LocalDateTime.class),
                  row.get("updated_at", LocalDateTime.class),
                  profile);
            })
        .all()
        .collectList()
        .block();
  }
}
