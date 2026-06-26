package io.spring.infrastructure.bookmark;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.core.user.User;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.ArticleBookmarksReadService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ArticleBookmarksReadServiceTest extends DbTestBase {
  @Autowired private ArticleBookmarksReadService readService;

  @Autowired private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUp() {
    jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "create table if not exists article_bookmarks ("
            + "article_id varchar(255) not null,"
            + "user_id varchar(255) not null,"
            + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "primary key (article_id, user_id))");
  }

  private void insertBookmark(String articleId, String userId, String createdAt) {
    jdbcTemplate.update(
        "insert into article_bookmarks (article_id, user_id, created_at) values (?, ?, ?)",
        articleId,
        userId,
        createdAt);
  }

  private void insertArticle(String id, String slug) {
    jdbcTemplate.update(
        "insert into articles (id, user_id, slug, title, description, body, created_at, updated_at)"
            + " values (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))",
        id,
        "author",
        slug,
        "title",
        "desc",
        "body");
  }

  @Test
  public void should_report_whether_user_bookmarked_article() {
    insertBookmark("a1", "u1", "2020-01-01 00:00:01");

    Assertions.assertTrue(readService.isUserBookmark("u1", "a1"));
    Assertions.assertFalse(readService.isUserBookmark("u1", "a2"));
    Assertions.assertFalse(readService.isUserBookmark("u2", "a1"));
  }

  @Test
  public void should_return_only_current_users_bookmarks() {
    insertArticle("a1", "slug-1");
    insertArticle("a2", "slug-2");

    User currentUser = new User("cur@example.com", "cur", "123", "", "");
    insertBookmark("a1", currentUser.getId(), "2020-01-01 00:00:01");
    insertBookmark("a2", "other-user", "2020-01-01 00:00:02");

    Set<String> bookmarks = readService.userBookmarks(Arrays.asList("a1", "a2"), currentUser);

    Assertions.assertEquals(1, bookmarks.size());
    Assertions.assertTrue(bookmarks.contains("a1"));
    Assertions.assertFalse(bookmarks.contains("a2"));
  }

  @Test
  public void should_return_bookmarked_ids_ordered_by_created_at_desc_with_cursor() {
    String userId = "cursor-user";
    insertBookmark("a1", userId, "2020-01-01 00:00:01");
    insertBookmark("a2", userId, "2020-01-01 00:00:02");
    insertBookmark("a3", userId, "2020-01-01 00:00:03");
    insertBookmark("a4", userId, "2020-01-01 00:00:04");

    List<String> firstPage =
        readService.findUserBookmarkedArticleIdsWithCursor(
            userId, new CursorPageParameter<>(null, 2, Direction.NEXT));
    Assertions.assertEquals(Arrays.asList("a4", "a3", "a2"), firstPage);

    List<String> nextPage =
        readService.findUserBookmarkedArticleIdsWithCursor(
            userId, new CursorPageParameter<>("2020-01-01 00:00:03", 2, Direction.NEXT));
    Assertions.assertEquals(Arrays.asList("a2", "a1"), nextPage);

    List<String> prevPage =
        readService.findUserBookmarkedArticleIdsWithCursor(
            userId, new CursorPageParameter<>("2020-01-01 00:00:02", 2, Direction.PREV));
    Assertions.assertEquals(Arrays.asList("a4", "a3"), prevPage);
  }

  @Test
  public void should_return_empty_for_user_without_bookmarks() {
    List<String> result =
        readService.findUserBookmarkedArticleIdsWithCursor(
            "nobody", new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(result.isEmpty());
  }
}
