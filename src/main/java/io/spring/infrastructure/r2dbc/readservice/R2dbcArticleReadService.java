package io.spring.infrastructure.r2dbc.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class R2dbcArticleReadService implements ArticleReadService {

  private final DatabaseClient db;

  public R2dbcArticleReadService(DatabaseClient db) {
    this.db = db;
  }

  @Override
  public ArticleData findById(String id) {
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at, "
                + "u.id as user_id, u.username, u.bio, u.image "
                + "FROM articles a "
                + "JOIN users u ON a.user_id = u.id "
                + "WHERE a.id = :id")
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
              return new ArticleData(
                  row.get("id", String.class),
                  row.get("slug", String.class),
                  row.get("title", String.class),
                  row.get("description", String.class),
                  row.get("body", String.class),
                  false,
                  0,
                  row.get("created_at", LocalDateTime.class),
                  row.get("updated_at", LocalDateTime.class),
                  new ArrayList<>(),
                  profile);
            })
        .one()
        .map(this::loadTags)
        .block();
  }

  @Override
  public ArticleData findBySlug(String slug) {
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at, "
                + "u.id as user_id, u.username, u.bio, u.image "
                + "FROM articles a "
                + "JOIN users u ON a.user_id = u.id "
                + "WHERE a.slug = :slug")
        .bind("slug", slug)
        .map(
            (row, metadata) -> {
              ProfileData profile =
                  new ProfileData(
                      row.get("user_id", String.class),
                      row.get("username", String.class),
                      row.get("bio", String.class),
                      row.get("image", String.class),
                      false);
              return new ArticleData(
                  row.get("id", String.class),
                  row.get("slug", String.class),
                  row.get("title", String.class),
                  row.get("description", String.class),
                  row.get("body", String.class),
                  false,
                  0,
                  row.get("created_at", LocalDateTime.class),
                  row.get("updated_at", LocalDateTime.class),
                  new ArrayList<>(),
                  profile);
            })
        .one()
        .map(this::loadTags)
        .block();
  }

  private ArticleData loadTags(ArticleData article) {
    if (article == null) {
      return null;
    }
    List<String> tags =
        db.sql(
                "SELECT t.name FROM tags t "
                    + "JOIN article_tags at ON t.id = at.tag_id "
                    + "WHERE at.article_id = :articleId")
            .bind("articleId", article.getId())
            .map((row, metadata) -> row.get("name", String.class))
            .all()
            .collectList()
            .block();
    article.setTagList(tags != null ? tags : new ArrayList<>());
    return article;
  }

  @Override
  public List<String> queryArticles(String tag, String author, String favoritedBy, Page page) {
    StringBuilder sql = new StringBuilder("SELECT a.id FROM articles a ");
    sql.append("JOIN users u ON a.user_id = u.id ");

    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append("JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id ");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          "JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id = fu.id ");
      conditions.add("fu.username = :favoritedBy");
    }

    if (!conditions.isEmpty()) {
      sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
    }

    sql.append("GROUP BY a.id, a.created_at ORDER BY a.created_at DESC ");
    sql.append("LIMIT :limit OFFSET :offset");

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) {
      spec = spec.bind("tag", tag);
    }
    if (author != null && !author.isEmpty()) {
      spec = spec.bind("author", author);
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      spec = spec.bind("favoritedBy", favoritedBy);
    }
    spec = spec.bind("limit", page.getLimit()).bind("offset", page.getOffset());

    return spec.map((row, metadata) -> row.get("id", String.class)).all().collectList().block();
  }

  @Override
  public int countArticle(String tag, String author, String favoritedBy) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT a.id) as cnt FROM articles a ");
    sql.append("JOIN users u ON a.user_id = u.id ");

    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append("JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id ");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          "JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id = fu.id ");
      conditions.add("fu.username = :favoritedBy");
    }

    if (!conditions.isEmpty()) {
      sql.append("WHERE ").append(String.join(" AND ", conditions));
    }

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) {
      spec = spec.bind("tag", tag);
    }
    if (author != null && !author.isEmpty()) {
      spec = spec.bind("author", author);
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      spec = spec.bind("favoritedBy", favoritedBy);
    }

    Long count = spec.map((row, metadata) -> row.get("cnt", Long.class)).one().block();
    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<ArticleData> findArticles(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) {
      return new ArrayList<>();
    }

    String placeholders =
        articleIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));

    List<ArticleData> articles =
        db.sql(
                "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at, "
                    + "u.id as user_id, u.username, u.bio, u.image "
                    + "FROM articles a "
                    + "JOIN users u ON a.user_id = u.id "
                    + "WHERE a.id IN ("
                    + placeholders
                    + ") "
                    + "ORDER BY a.created_at DESC")
            .map(
                (row, metadata) -> {
                  ProfileData profile =
                      new ProfileData(
                          row.get("user_id", String.class),
                          row.get("username", String.class),
                          row.get("bio", String.class),
                          row.get("image", String.class),
                          false);
                  return new ArticleData(
                      row.get("id", String.class),
                      row.get("slug", String.class),
                      row.get("title", String.class),
                      row.get("description", String.class),
                      row.get("body", String.class),
                      false,
                      0,
                      row.get("created_at", LocalDateTime.class),
                      row.get("updated_at", LocalDateTime.class),
                      new ArrayList<>(),
                      profile);
                })
            .all()
            .collectList()
            .block();

    if (articles != null) {
      articles.forEach(this::loadTags);
    }
    return articles != null ? articles : new ArrayList<>();
  }

  @Override
  public List<ArticleData> findArticlesOfAuthors(List<String> authors, Page page) {
    if (authors == null || authors.isEmpty()) {
      return new ArrayList<>();
    }

    String placeholders =
        authors.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));

    List<ArticleData> articles =
        db.sql(
                "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at, "
                    + "u.id as user_id, u.username, u.bio, u.image "
                    + "FROM articles a "
                    + "JOIN users u ON a.user_id = u.id "
                    + "WHERE a.user_id IN ("
                    + placeholders
                    + ") "
                    + "ORDER BY a.created_at DESC "
                    + "LIMIT :limit OFFSET :offset")
            .bind("limit", page.getLimit())
            .bind("offset", page.getOffset())
            .map(
                (row, metadata) -> {
                  ProfileData profile =
                      new ProfileData(
                          row.get("user_id", String.class),
                          row.get("username", String.class),
                          row.get("bio", String.class),
                          row.get("image", String.class),
                          false);
                  return new ArticleData(
                      row.get("id", String.class),
                      row.get("slug", String.class),
                      row.get("title", String.class),
                      row.get("description", String.class),
                      row.get("body", String.class),
                      false,
                      0,
                      row.get("created_at", LocalDateTime.class),
                      row.get("updated_at", LocalDateTime.class),
                      new ArrayList<>(),
                      profile);
                })
            .all()
            .collectList()
            .block();

    if (articles != null) {
      articles.forEach(this::loadTags);
    }
    return articles != null ? articles : new ArrayList<>();
  }

  @Override
  public List<ArticleData> findArticlesOfAuthorsWithCursor(
      List<String> authors, CursorPageParameter page) {
    if (authors == null || authors.isEmpty()) {
      return new ArrayList<>();
    }

    String placeholders =
        authors.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
    String direction = page.getDirection().name().equals("NEXT") ? "<" : ">";
    String order = page.getDirection().name().equals("NEXT") ? "DESC" : "ASC";

    StringBuilder sql =
        new StringBuilder(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at, "
                + "u.id as user_id, u.username, u.bio, u.image "
                + "FROM articles a "
                + "JOIN users u ON a.user_id = u.id "
                + "WHERE a.user_id IN ("
                + placeholders
                + ") ");

    if (page.getCursor() != null) {
      sql.append("AND a.updated_at ").append(direction).append(" :cursor ");
    }
    sql.append("ORDER BY a.updated_at ").append(order).append(" LIMIT :limit");

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString()).bind("limit", page.getLimit());
    if (page.getCursor() != null) {
      spec = spec.bind("cursor", page.getCursor());
    }

    List<ArticleData> articles =
        spec.map(
                (row, metadata) -> {
                  ProfileData profile =
                      new ProfileData(
                          row.get("user_id", String.class),
                          row.get("username", String.class),
                          row.get("bio", String.class),
                          row.get("image", String.class),
                          false);
                  return new ArticleData(
                      row.get("id", String.class),
                      row.get("slug", String.class),
                      row.get("title", String.class),
                      row.get("description", String.class),
                      row.get("body", String.class),
                      false,
                      0,
                      row.get("created_at", LocalDateTime.class),
                      row.get("updated_at", LocalDateTime.class),
                      new ArrayList<>(),
                      profile);
                })
            .all()
            .collectList()
            .block();

    if (articles != null) {
      articles.forEach(this::loadTags);
    }
    return articles != null ? articles : new ArrayList<>();
  }

  @Override
  public int countFeedSize(List<String> authors) {
    if (authors == null || authors.isEmpty()) {
      return 0;
    }

    String placeholders =
        authors.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));

    Long count =
        db.sql("SELECT COUNT(*) as cnt FROM articles WHERE user_id IN (" + placeholders + ")")
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .block();
    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<String> findArticlesWithCursor(
      String tag, String author, String favoritedBy, CursorPageParameter page) {
    StringBuilder sql = new StringBuilder("SELECT a.id, a.updated_at FROM articles a ");
    sql.append("JOIN users u ON a.user_id = u.id ");

    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append("JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id ");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          "JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id = fu.id ");
      conditions.add("fu.username = :favoritedBy");
    }

    String direction = page.getDirection().name().equals("NEXT") ? "<" : ">";
    String order = page.getDirection().name().equals("NEXT") ? "DESC" : "ASC";

    if (page.getCursor() != null) {
      conditions.add("a.updated_at " + direction + " :cursor");
    }

    if (!conditions.isEmpty()) {
      sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
    }

    sql.append("GROUP BY a.id, a.updated_at ORDER BY a.updated_at ").append(order).append(" ");
    sql.append("LIMIT :limit");

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) {
      spec = spec.bind("tag", tag);
    }
    if (author != null && !author.isEmpty()) {
      spec = spec.bind("author", author);
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      spec = spec.bind("favoritedBy", favoritedBy);
    }
    if (page.getCursor() != null) {
      spec = spec.bind("cursor", page.getCursor());
    }
    spec = spec.bind("limit", page.getLimit());

    return spec.map((row, metadata) -> row.get("id", String.class)).all().collectList().block();
  }
}
