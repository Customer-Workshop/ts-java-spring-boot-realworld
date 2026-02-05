package io.spring.infrastructure.repository;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcUserRepository implements UserRepository {
  private final DatabaseClient databaseClient;

  public R2dbcUserRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<User> save(User user) {
    return findById(user.getId()).flatMap(existing -> update(user)).switchIfEmpty(insert(user));
  }

  private Mono<User> insert(User user) {
    return databaseClient
        .sql(
            "INSERT INTO users (id, username, email, password, bio, image) VALUES (:id, :username, :email, :password, :bio, :image)")
        .bind("id", user.getId())
        .bind("username", user.getUsername())
        .bind("email", user.getEmail())
        .bind("password", user.getPassword())
        .bindNull("bio", String.class)
        .bindNull("image", String.class)
        .fetch()
        .rowsUpdated()
        .thenReturn(user);
  }

  private Mono<User> update(User user) {
    return databaseClient
        .sql(
            "UPDATE users SET username = :username, email = :email, password = :password, bio = :bio, image = :image WHERE id = :id")
        .bind("id", user.getId())
        .bind("username", user.getUsername())
        .bind("email", user.getEmail())
        .bind("password", user.getPassword())
        .bind("bio", user.getBio() != null ? user.getBio() : "")
        .bind("image", user.getImage() != null ? user.getImage() : "")
        .fetch()
        .rowsUpdated()
        .thenReturn(user);
  }

  @Override
  public Mono<User> findById(String id) {
    return databaseClient
        .sql("SELECT * FROM users WHERE id = :id")
        .bind("id", id)
        .map(
            row -> {
              User user = new User();
              setUserFields(user, row);
              return user;
            })
        .one();
  }

  @Override
  public Mono<User> findByUsername(String username) {
    return databaseClient
        .sql("SELECT * FROM users WHERE username = :username")
        .bind("username", username)
        .map(
            row -> {
              User user = new User();
              setUserFields(user, row);
              return user;
            })
        .one();
  }

  @Override
  public Mono<User> findByEmail(String email) {
    return databaseClient
        .sql("SELECT * FROM users WHERE email = :email")
        .bind("email", email)
        .map(
            row -> {
              User user = new User();
              setUserFields(user, row);
              return user;
            })
        .one();
  }

  private void setUserFields(User user, io.r2dbc.spi.Readable row) {
    try {
      var idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(user, row.get("id", String.class));

      var emailField = User.class.getDeclaredField("email");
      emailField.setAccessible(true);
      emailField.set(user, row.get("email", String.class));

      var usernameField = User.class.getDeclaredField("username");
      usernameField.setAccessible(true);
      usernameField.set(user, row.get("username", String.class));

      var passwordField = User.class.getDeclaredField("password");
      passwordField.setAccessible(true);
      passwordField.set(user, row.get("password", String.class));

      var bioField = User.class.getDeclaredField("bio");
      bioField.setAccessible(true);
      bioField.set(user, row.get("bio", String.class));

      var imageField = User.class.getDeclaredField("image");
      imageField.setAccessible(true);
      imageField.set(user, row.get("image", String.class));
    } catch (Exception e) {
      throw new RuntimeException("Failed to set user fields", e);
    }
  }

  @Override
  public Mono<Void> saveRelation(FollowRelation followRelation) {
    return findRelation(followRelation.getUserId(), followRelation.getTargetId())
        .switchIfEmpty(
            databaseClient
                .sql("INSERT INTO follows (user_id, follow_id) VALUES (:userId, :followId)")
                .bind("userId", followRelation.getUserId())
                .bind("followId", followRelation.getTargetId())
                .fetch()
                .rowsUpdated()
                .then(Mono.empty()))
        .then();
  }

  @Override
  public Mono<FollowRelation> findRelation(String userId, String targetId) {
    return databaseClient
        .sql("SELECT * FROM follows WHERE user_id = :userId AND follow_id = :followId")
        .bind("userId", userId)
        .bind("followId", targetId)
        .map(
            row ->
                new FollowRelation(
                    row.get("user_id", String.class), row.get("follow_id", String.class)))
        .one();
  }

  @Override
  public Mono<Void> removeRelation(FollowRelation followRelation) {
    return databaseClient
        .sql("DELETE FROM follows WHERE user_id = :userId AND follow_id = :followId")
        .bind("userId", followRelation.getUserId())
        .bind("followId", followRelation.getTargetId())
        .fetch()
        .rowsUpdated()
        .then();
  }
}
