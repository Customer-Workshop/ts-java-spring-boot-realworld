package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.BookmarkedArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({ArticleQueryService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class})
public class ArticleBookmarkQueryServiceTest extends DbTestBase {
  @Autowired private ArticleQueryService queryService;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private User user;
  private User other;

  @BeforeEach
  public void setUp() {
    jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "create table if not exists article_bookmarks ("
            + "article_id varchar(255) not null,"
            + "user_id varchar(255) not null,"
            + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "primary key (article_id, user_id))");
    jdbcTemplate.execute("delete from article_bookmarks");

    user = new User("reader@example.com", "reader", "123", "", "");
    userRepository.save(user);
    other = new User("other@example.com", "other", "123", "", "");
    userRepository.save(other);
  }

  /**
   * Persists an article whose own create/update time is intentionally unrelated to the bookmark
   * time, so the tests can prove ordering/cursoring is anchored on bookmark time only.
   */
  private Article article(String title, DateTime articleCreatedAt) {
    Article article =
        new Article(title, "desc", "body", Arrays.asList("java"), user.getId(), articleCreatedAt);
    articleRepository.save(article);
    return article;
  }

  private void bookmark(String articleId, String userId, String createdAt) {
    jdbcTemplate.update(
        "insert into article_bookmarks (article_id, user_id, created_at) values (?, ?, ?)",
        articleId,
        userId,
        createdAt);
  }

  private List<String> ids(List<ArticleData> articles) {
    return articles.stream().map(ArticleData::getId).collect(Collectors.toList());
  }

  @Test
  public void should_return_bookmarks_newest_bookmarked_first() {
    // article create order is the REVERSE of bookmark order on purpose.
    Article a1 = article("oldest-article", new DateTime("2020-01-01T00:00:00Z"));
    Article a2 = article("middle-article", new DateTime("2020-02-01T00:00:00Z"));
    Article a3 = article("newest-article", new DateTime("2020-03-01T00:00:00Z"));

    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:03"); // bookmarked last
    bookmark(a2.getId(), user.getId(), "2021-05-01 10:00:02");
    bookmark(a3.getId(), user.getId(), "2021-05-01 10:00:01"); // bookmarked first

    ArticleDataList list = queryService.findUserBookmarks(user, new Page(0, 20));

    Assertions.assertEquals(3, list.getCount());
    Assertions.assertEquals(
        Arrays.asList(a1.getId(), a2.getId(), a3.getId()), ids(list.getArticleDatas()));
  }

  @Test
  public void should_paginate_bookmarks_with_offset_and_limit() {
    Article a1 = article("a1", new DateTime("2020-03-01T00:00:00Z"));
    Article a2 = article("a2", new DateTime("2020-02-01T00:00:00Z"));
    Article a3 = article("a3", new DateTime("2020-01-01T00:00:00Z"));

    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:01");
    bookmark(a2.getId(), user.getId(), "2021-05-01 10:00:02");
    bookmark(a3.getId(), user.getId(), "2021-05-01 10:00:03");

    ArticleDataList firstPage = queryService.findUserBookmarks(user, new Page(0, 2));
    Assertions.assertEquals(3, firstPage.getCount());
    Assertions.assertEquals(
        Arrays.asList(a3.getId(), a2.getId()), ids(firstPage.getArticleDatas()));

    ArticleDataList secondPage = queryService.findUserBookmarks(user, new Page(2, 2));
    Assertions.assertEquals(3, secondPage.getCount());
    Assertions.assertEquals(Arrays.asList(a1.getId()), ids(secondPage.getArticleDatas()));
  }

  @Test
  public void should_never_return_other_users_bookmarks() {
    Article mine = article("mine", new DateTime("2020-01-01T00:00:00Z"));
    Article theirs = article("theirs", new DateTime("2020-01-02T00:00:00Z"));

    bookmark(mine.getId(), user.getId(), "2021-05-01 10:00:01");
    bookmark(theirs.getId(), other.getId(), "2021-05-01 10:00:09");

    ArticleDataList list = queryService.findUserBookmarks(user, new Page(0, 20));
    Assertions.assertEquals(1, list.getCount());
    Assertions.assertEquals(Arrays.asList(mine.getId()), ids(list.getArticleDatas()));
  }

  @Test
  public void should_return_empty_reading_list() {
    ArticleDataList list = queryService.findUserBookmarks(user, new Page(0, 20));
    Assertions.assertEquals(0, list.getCount());
    Assertions.assertTrue(list.getArticleDatas().isEmpty());

    CursorPager<BookmarkedArticleData> cursor =
        queryService.findUserBookmarksWithCursor(
            user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(cursor.getData().isEmpty());
    Assertions.assertNull(cursor.getStartCursor());
    Assertions.assertFalse(cursor.hasNext());
  }

  @Test
  public void should_cursor_bookmarks_newest_first_anchored_on_bookmark_time() {
    Article a1 = article("oldest-article", new DateTime("2020-01-01T00:00:00Z"));
    Article a2 = article("middle-article", new DateTime("2020-02-01T00:00:00Z"));
    Article a3 = article("newest-article", new DateTime("2020-03-01T00:00:00Z"));

    // bookmark order is independent of article create time.
    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:03");
    bookmark(a2.getId(), user.getId(), "2021-05-01 10:00:02");
    bookmark(a3.getId(), user.getId(), "2021-05-01 10:00:01");

    CursorPager<BookmarkedArticleData> first =
        queryService.findUserBookmarksWithCursor(
            user, new CursorPageParameter<>(null, 2, Direction.NEXT));

    Assertions.assertEquals(2, first.getData().size());
    Assertions.assertEquals(a1.getId(), first.getData().get(0).getArticle().getId());
    Assertions.assertEquals(a2.getId(), first.getData().get(1).getArticle().getId());
    Assertions.assertTrue(first.hasNext());

    // the emitted cursor must be the bookmark created_at, not the article updatedAt.
    long expectedEndMillis = new DateTime("2021-05-01T10:00:02Z").getMillis();
    Assertions.assertEquals(String.valueOf(expectedEndMillis), first.getEndCursor().toString());
  }

  @Test
  public void should_page_with_after_cursor_without_overlap() {
    Article a1 = article("oldest-article", new DateTime("2020-01-01T00:00:00Z"));
    Article a2 = article("middle-article", new DateTime("2020-02-01T00:00:00Z"));
    Article a3 = article("newest-article", new DateTime("2020-03-01T00:00:00Z"));

    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:03");
    bookmark(a2.getId(), user.getId(), "2021-05-01 10:00:02");
    bookmark(a3.getId(), user.getId(), "2021-05-01 10:00:01");

    CursorPager<BookmarkedArticleData> first =
        queryService.findUserBookmarksWithCursor(
            user, new CursorPageParameter<>(null, 2, Direction.NEXT));

    CursorPager<BookmarkedArticleData> next =
        queryService.findUserBookmarksWithCursor(
            user,
            new CursorPageParameter<>(
                DateTimeCursor.parse(first.getEndCursor().toString()), 2, Direction.NEXT));

    Assertions.assertEquals(1, next.getData().size());
    Assertions.assertEquals(a3.getId(), next.getData().get(0).getArticle().getId());
    Assertions.assertFalse(next.hasNext());
  }

  @Test
  public void should_page_backwards_with_before_cursor() {
    Article a1 = article("a1", new DateTime("2020-01-01T00:00:00Z"));
    Article a2 = article("a2", new DateTime("2020-02-01T00:00:00Z"));
    Article a3 = article("a3", new DateTime("2020-03-01T00:00:00Z"));
    Article a4 = article("a4", new DateTime("2020-04-01T00:00:00Z"));

    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:04");
    bookmark(a2.getId(), user.getId(), "2021-05-01 10:00:03");
    bookmark(a3.getId(), user.getId(), "2021-05-01 10:00:02");
    bookmark(a4.getId(), user.getId(), "2021-05-01 10:00:01");

    // before the oldest-bookmarked (a4) -> walking back toward newer bookmarks, newest-first.
    CursorPager<BookmarkedArticleData> prev =
        queryService.findUserBookmarksWithCursor(
            user,
            new CursorPageParameter<>(
                DateTimeCursor.parse(
                    String.valueOf(new DateTime("2021-05-01T10:00:01Z").getMillis())),
                2,
                Direction.PREV));

    Assertions.assertEquals(2, prev.getData().size());
    Assertions.assertEquals(a2.getId(), prev.getData().get(0).getArticle().getId());
    Assertions.assertEquals(a3.getId(), prev.getData().get(1).getArticle().getId());
    Assertions.assertTrue(prev.hasPrevious());
    Assertions.assertFalse(prev.hasNext());
  }

  @Test
  public void should_drop_bookmarks_for_deleted_articles() {
    Article a1 = article("kept", new DateTime("2020-01-01T00:00:00Z"));

    bookmark(a1.getId(), user.getId(), "2021-05-01 10:00:01");
    bookmark("missing-article-id", user.getId(), "2021-05-01 10:00:02");

    ArticleDataList list = queryService.findUserBookmarks(user, new Page(0, 20));
    // count reflects raw bookmarks; the dangling one is filtered from the result set.
    Assertions.assertEquals(2, list.getCount());
    Assertions.assertEquals(Arrays.asList(a1.getId()), ids(list.getArticleDatas()));
  }

  @Test
  public void should_return_empty_when_every_bookmarked_article_is_deleted() {
    bookmark("missing-1", user.getId(), "2021-05-01 10:00:01");
    bookmark("missing-2", user.getId(), "2021-05-01 10:00:02");

    // REST offset/limit path: no fillExtraInfo over an empty list (no empty IN () SQL).
    ArticleDataList list = queryService.findUserBookmarks(user, new Page(0, 20));
    Assertions.assertTrue(list.getArticleDatas().isEmpty());
    Assertions.assertEquals(2, list.getCount());

    // Cursor path: same guard.
    CursorPager<BookmarkedArticleData> page =
        queryService.findUserBookmarksWithCursor(
            user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(page.getData().isEmpty());
  }
}
