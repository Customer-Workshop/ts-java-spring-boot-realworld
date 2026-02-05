package io.spring.infrastructure.r2dbc.readservice;

import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcUserRelationshipQueryService implements UserRelationshipQueryService {

  private final DatabaseClient db;

  public R2dbcUserRelationshipQueryService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public boolean isUserFollowing(String userId, String anotherUserId) {
    Long count =
        db.sql(
                "SELECT COUNT(*) as cnt FROM follows WHERE user_id = :userId AND follow_id = :anotherUserId")
            .bind("userId", userId)
            .bind("anotherUserId", anotherUserId)
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .block();
    return count != null && count > 0;
  }

  @Override
  public Set<String> followingAuthors(String userId, List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return new HashSet<>();
    }
    String placeholders = ids.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
    List<String> following =
        db.sql(
                "SELECT follow_id FROM follows WHERE user_id = :userId AND follow_id IN ("
                    + placeholders
                    + ")")
            .bind("userId", userId)
            .map((row, metadata) -> row.get("follow_id", String.class))
            .all()
            .collectList()
            .block();
    return following != null ? new HashSet<>(following) : new HashSet<>();
  }

  @Override
  public List<String> followedUsers(String userId) {
    List<String> followed =
        db.sql("SELECT follow_id FROM follows WHERE user_id = :userId")
            .bind("userId", userId)
            .map((row, metadata) -> row.get("follow_id", String.class))
            .all()
            .collectList()
            .block();
    return followed != null ? followed : List.of();
  }
}
