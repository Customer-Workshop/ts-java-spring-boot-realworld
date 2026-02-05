package io.spring.infrastructure.repository;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcArticleRepository implements ArticleRepository {
  private final DatabaseClient databaseClient;

  public R2dbcArticleRepository(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public Mono<Article> save(Article article) {
    return findById(article.getId())
        .flatMap(existing -> update(article))
        .switchIfEmpty(insert(article));
  }

  private Mono<Article> insert(Article article) {
    return databaseClient
        .sql(
            "INSERT INTO articles (id, user_id, slug, title, description, body, created_at, updated_at) "
                + "VALUES (:id, :userId, :slug, :title, :description, :body, :createdAt, :updatedAt)")
        .bind("id", article.getId())
        .bind("userId", article.getUserId())
        .bind("slug", article.getSlug())
        .bind("title", article.getTitle())
        .bind("description", article.getDescription())
        .bind("body", article.getBody())
        .bind("createdAt", article.getCreatedAt())
        .bind("updatedAt", article.getUpdatedAt())
        .fetch()
        .rowsUpdated()
        .then(saveTags(article))
        .thenReturn(article);
  }

  private Mono<Void> saveTags(Article article) {
    if (article.getTags() == null || article.getTags().isEmpty()) {
      return Mono.empty();
    }

    return Flux.fromIterable(article.getTags())
        .flatMap(
            tag ->
                saveTag(tag)
                    .flatMap(savedTag -> saveArticleTagRelation(article.getId(), savedTag.getId())))
        .then();
  }

  private Mono<Tag> saveTag(Tag tag) {
    return databaseClient
        .sql("SELECT * FROM tags WHERE name = :name")
        .bind("name", tag.getName())
        .map(
            row -> {
              try {
                Tag t = new Tag();
                var idField = Tag.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(t, row.get("id", String.class));
                var nameField = Tag.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(t, row.get("name", String.class));
                return t;
              } catch (Exception e) {
                throw new RuntimeException("Failed to map tag", e);
              }
            })
        .one()
        .switchIfEmpty(
            databaseClient
                .sql("INSERT INTO tags (id, name) VALUES (:id, :name)")
                .bind("id", tag.getId())
                .bind("name", tag.getName())
                .fetch()
                .rowsUpdated()
                .thenReturn(tag));
  }

  private Mono<Void> saveArticleTagRelation(String articleId, String tagId) {
    return databaseClient
        .sql("INSERT INTO article_tags (article_id, tag_id) VALUES (:articleId, :tagId)")
        .bind("articleId", articleId)
        .bind("tagId", tagId)
        .fetch()
        .rowsUpdated()
        .then();
  }

  private Mono<Article> update(Article article) {
    return databaseClient
        .sql(
            "UPDATE articles SET slug = :slug, title = :title, description = :description, "
                + "body = :body, updated_at = :updatedAt WHERE id = :id")
        .bind("id", article.getId())
        .bind("slug", article.getSlug())
        .bind("title", article.getTitle())
        .bind("description", article.getDescription())
        .bind("body", article.getBody())
        .bind("updatedAt", article.getUpdatedAt())
        .fetch()
        .rowsUpdated()
        .thenReturn(article);
  }

  @Override
  public Mono<Article> findById(String id) {
    return databaseClient
        .sql("SELECT * FROM articles WHERE id = :id")
        .bind("id", id)
        .map(row -> mapRowToArticle(row))
        .one()
        .flatMap(this::loadTags);
  }

  @Override
  public Mono<Article> findBySlug(String slug) {
    return databaseClient
        .sql("SELECT * FROM articles WHERE slug = :slug")
        .bind("slug", slug)
        .map(row -> mapRowToArticle(row))
        .one()
        .flatMap(this::loadTags);
  }

  private Article mapRowToArticle(io.r2dbc.spi.Readable row) {
    try {
      Article article = new Article();
      var idField = Article.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(article, row.get("id", String.class));

      var userIdField = Article.class.getDeclaredField("userId");
      userIdField.setAccessible(true);
      userIdField.set(article, row.get("user_id", String.class));

      var slugField = Article.class.getDeclaredField("slug");
      slugField.setAccessible(true);
      slugField.set(article, row.get("slug", String.class));

      var titleField = Article.class.getDeclaredField("title");
      titleField.setAccessible(true);
      titleField.set(article, row.get("title", String.class));

      var descriptionField = Article.class.getDeclaredField("description");
      descriptionField.setAccessible(true);
      descriptionField.set(article, row.get("description", String.class));

      var bodyField = Article.class.getDeclaredField("body");
      bodyField.setAccessible(true);
      bodyField.set(article, row.get("body", String.class));

      var createdAtField = Article.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(article, row.get("created_at", LocalDateTime.class));

      var updatedAtField = Article.class.getDeclaredField("updatedAt");
      updatedAtField.setAccessible(true);
      updatedAtField.set(article, row.get("updated_at", LocalDateTime.class));

      var tagsField = Article.class.getDeclaredField("tags");
      tagsField.setAccessible(true);
      tagsField.set(article, new ArrayList<Tag>());

      return article;
    } catch (Exception e) {
      throw new RuntimeException("Failed to map article", e);
    }
  }

  private Mono<Article> loadTags(Article article) {
    return databaseClient
        .sql(
            "SELECT t.* FROM tags t JOIN article_tags at ON t.id = at.tag_id WHERE at.article_id = :articleId")
        .bind("articleId", article.getId())
        .map(
            row -> {
              try {
                Tag tag = new Tag();
                var idField = Tag.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(tag, row.get("id", String.class));
                var nameField = Tag.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(tag, row.get("name", String.class));
                return tag;
              } catch (Exception e) {
                throw new RuntimeException("Failed to map tag", e);
              }
            })
        .all()
        .collectList()
        .map(
            tags -> {
              try {
                var tagsField = Article.class.getDeclaredField("tags");
                tagsField.setAccessible(true);
                tagsField.set(article, tags);
                return article;
              } catch (Exception e) {
                throw new RuntimeException("Failed to set tags", e);
              }
            });
  }

  @Override
  public Mono<Void> remove(Article article) {
    return databaseClient
        .sql("DELETE FROM article_tags WHERE article_id = :articleId")
        .bind("articleId", article.getId())
        .fetch()
        .rowsUpdated()
        .then(
            databaseClient
                .sql("DELETE FROM article_favorites WHERE article_id = :articleId")
                .bind("articleId", article.getId())
                .fetch()
                .rowsUpdated())
        .then(
            databaseClient
                .sql("DELETE FROM comments WHERE article_id = :articleId")
                .bind("articleId", article.getId())
                .fetch()
                .rowsUpdated())
        .then(
            databaseClient
                .sql("DELETE FROM articles WHERE id = :id")
                .bind("id", article.getId())
                .fetch()
                .rowsUpdated())
        .then();
  }
}
