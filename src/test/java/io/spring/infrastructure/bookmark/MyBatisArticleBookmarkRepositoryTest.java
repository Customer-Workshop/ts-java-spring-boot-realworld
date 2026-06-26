package io.spring.infrastructure.bookmark;

import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleBookmarkRepository;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({MyBatisArticleBookmarkRepository.class})
public class MyBatisArticleBookmarkRepositoryTest extends DbTestBase {
  @Autowired private ArticleBookmarkRepository articleBookmarkRepository;

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

  @Test
  public void should_save_and_fetch_articleBookmark_success() {
    ArticleBookmark bookmark = new ArticleBookmark("123", "456");
    articleBookmarkRepository.save(bookmark);
    Assertions.assertTrue(articleBookmarkRepository.find("123", "456").isPresent());
  }

  @Test
  public void should_be_idempotent_when_saving_same_bookmark_twice() {
    ArticleBookmark bookmark = new ArticleBookmark("123", "456");
    articleBookmarkRepository.save(bookmark);
    articleBookmarkRepository.save(bookmark);

    Integer rows =
        jdbcTemplate.queryForObject(
            "select count(1) from article_bookmarks where article_id = ? and user_id = ?",
            Integer.class,
            "123",
            "456");
    Assertions.assertEquals(1, rows);
  }

  @Test
  public void should_remove_bookmark_success() {
    ArticleBookmark bookmark = new ArticleBookmark("123", "456");
    articleBookmarkRepository.save(bookmark);
    articleBookmarkRepository.remove(bookmark);
    Assertions.assertFalse(articleBookmarkRepository.find("123", "456").isPresent());
  }

  @Test
  public void should_be_noop_when_removing_non_existent_bookmark() {
    ArticleBookmark bookmark = new ArticleBookmark("does-not", "exist");
    Assertions.assertDoesNotThrow(() -> articleBookmarkRepository.remove(bookmark));
    Assertions.assertFalse(articleBookmarkRepository.find("does-not", "exist").isPresent());
  }
}
