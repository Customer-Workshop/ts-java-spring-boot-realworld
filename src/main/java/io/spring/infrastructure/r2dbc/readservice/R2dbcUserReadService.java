package io.spring.infrastructure.r2dbc.readservice;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcUserReadService implements UserReadService {

  private final DatabaseClient db;

  public R2dbcUserReadService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public UserData findByUsername(String username) {
    return db.sql("SELECT id, email, username, bio, image FROM users WHERE username = :username")
        .bind("username", username)
        .map(
            (row, metadata) ->
                new UserData(
                    row.get("id", String.class),
                    row.get("email", String.class),
                    row.get("username", String.class),
                    row.get("bio", String.class),
                    row.get("image", String.class)))
        .one()
        .block();
  }

  @Override
  public UserData findById(String id) {
    return db.sql("SELECT id, email, username, bio, image FROM users WHERE id = :id")
        .bind("id", id)
        .map(
            (row, metadata) ->
                new UserData(
                    row.get("id", String.class),
                    row.get("email", String.class),
                    row.get("username", String.class),
                    row.get("bio", String.class),
                    row.get("image", String.class)))
        .one()
        .block();
  }
}
