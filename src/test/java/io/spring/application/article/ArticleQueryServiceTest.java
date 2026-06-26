package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleBookmarkRepository;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.Optional;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({
  ArticleQueryService.class,
  MyBatisUserRepository.class,
  MyBatisArticleRepository.class,
  MyBatisArticleFavoriteRepository.class,
  MyBatisArticleBookmarkRepository.class
})
public class ArticleQueryServiceTest extends DbTestBase {
  @Autowired private ArticleQueryService queryService;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private ArticleBookmarkRepository articleBookmarkRepository;

  @Autowired private DataSource dataSource;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    // The test profile only runs Flyway V1 (spring.flyway.target=1), so the V3
    // article_bookmarks table is not present. Create it here so the bookmarked
    // read-model flag can be exercised against the real DB.
    new JdbcTemplate(dataSource)
        .execute(
            "create table if not exists article_bookmarks ("
                + "article_id varchar(255) not null,"
                + "user_id varchar(255) not null,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "primary key (article_id, user_id))");

    user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
    userRepository.save(user);
    article =
        new Article(
            "test", "desc", "body", Arrays.asList("java", "spring"), user.getId(), new DateTime());
    articleRepository.save(article);
  }

  @Test
  public void should_fetch_article_success() {
    Optional<ArticleData> optional = queryService.findById(article.getId(), user);
    Assertions.assertTrue(optional.isPresent());

    ArticleData fetched = optional.get();
    Assertions.assertEquals(fetched.getFavoritesCount(), 0);
    Assertions.assertFalse(fetched.isFavorited());
    Assertions.assertNotNull(fetched.getCreatedAt());
    Assertions.assertNotNull(fetched.getUpdatedAt());
    Assertions.assertTrue(fetched.getTagList().contains("java"));
  }

  @Test
  public void should_get_article_with_right_favorite_and_favorite_count() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

    Optional<ArticleData> optional = queryService.findById(article.getId(), anotherUser);
    Assertions.assertTrue(optional.isPresent());

    ArticleData articleData = optional.get();
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_default_bookmarked_to_false_when_not_bookmarked() {
    Optional<ArticleData> optional = queryService.findById(article.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(optional.get().isBookmarked());
  }

  @Test
  public void should_default_bookmarked_to_false_for_anonymous_user() {
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), user.getId()));

    Optional<ArticleData> optional = queryService.findById(article.getId(), null);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertFalse(optional.get().isBookmarked());
  }

  @Test
  public void should_get_article_with_bookmarked_true_when_user_bookmarked() {
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), user.getId()));

    ArticleData byId = queryService.findById(article.getId(), user).orElseThrow();
    Assertions.assertTrue(byId.isBookmarked());

    ArticleData bySlug = queryService.findBySlug(article.getSlug(), user).orElseThrow();
    Assertions.assertTrue(bySlug.isBookmarked());
  }

  @Test
  public void should_flip_bookmarked_flag_when_bookmark_added_and_removed() {
    Assertions.assertFalse(
        queryService.findById(article.getId(), user).orElseThrow().isBookmarked());

    ArticleBookmark bookmark = new ArticleBookmark(article.getId(), user.getId());
    articleBookmarkRepository.save(bookmark);
    Assertions.assertTrue(
        queryService.findById(article.getId(), user).orElseThrow().isBookmarked());

    articleBookmarkRepository.remove(bookmark);
    Assertions.assertFalse(
        queryService.findById(article.getId(), user).orElseThrow().isBookmarked());
  }

  @Test
  public void should_keep_bookmarked_only_for_current_user() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), anotherUser.getId()));

    Assertions.assertTrue(
        queryService.findById(article.getId(), anotherUser).orElseThrow().isBookmarked());
    Assertions.assertFalse(
        queryService.findById(article.getId(), user).orElseThrow().isBookmarked());
  }

  @Test
  public void should_keep_bookmarked_independent_of_favorited() {
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), user.getId()));

    ArticleData articleData = queryService.findById(article.getId(), user).orElseThrow();
    Assertions.assertTrue(articleData.isBookmarked());
    Assertions.assertFalse(articleData.isFavorited());
  }

  @Test
  public void should_set_bookmarked_flag_in_article_list() {
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), user.getId()));

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    ArticleData articleData =
        recentArticles.getArticleDatas().stream()
            .filter(a -> a.getId().equals(article.getId()))
            .findFirst()
            .orElseThrow();
    Assertions.assertTrue(articleData.isBookmarked());
  }

  @Test
  public void should_set_bookmarked_flag_in_article_list_by_cursor() {
    articleBookmarkRepository.save(new ArticleBookmark(article.getId(), user.getId()));

    CursorPager<ArticleData> recentArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
    ArticleData articleData =
        recentArticles.getData().stream()
            .filter(a -> a.getId().equals(article.getId()))
            .findFirst()
            .orElseThrow();
    Assertions.assertTrue(articleData.isBookmarked());
  }

  @Test
  public void should_set_bookmarked_flag_in_user_feed() {
    User author = new User("author@test.com", "author", "123", "", "");
    userRepository.save(author);
    Article authoredArticle =
        new Article("feed article", "desc", "body", Arrays.asList("test"), author.getId());
    articleRepository.save(authoredArticle);
    userRepository.saveRelation(new FollowRelation(user.getId(), author.getId()));
    articleBookmarkRepository.save(new ArticleBookmark(authoredArticle.getId(), user.getId()));

    ArticleDataList feed = queryService.findUserFeed(user, new Page());
    ArticleData articleData =
        feed.getArticleDatas().stream()
            .filter(a -> a.getId().equals(authoredArticle.getId()))
            .findFirst()
            .orElseThrow();
    Assertions.assertTrue(articleData.isBookmarked());
  }

  @Test
  public void should_get_default_article_list() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            new DateTime().minusHours(1));
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getCount(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList nodata =
        queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
    Assertions.assertEquals(nodata.getCount(), 2);
    Assertions.assertEquals(nodata.getArticleDatas().size(), 0);
  }

  @Test
  public void should_get_default_article_list_by_cursor() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            new DateTime().minusHours(1));
    articleRepository.save(anotherArticle);

    CursorPager<ArticleData> recentArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
    Assertions.assertEquals(recentArticles.getData().size(), 2);
    Assertions.assertEquals(recentArticles.getData().get(0).getId(), article.getId());

    CursorPager<ArticleData> nodata =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<DateTime>(
                DateTimeCursor.parse(recentArticles.getEndCursor().toString()), 20, Direction.NEXT),
            user);
    Assertions.assertEquals(nodata.getData().size(), 0);
    Assertions.assertEquals(nodata.getStartCursor(), null);

    CursorPager<ArticleData> prevArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.PREV), user);
    Assertions.assertEquals(prevArticles.getData().size(), 2);
  }

  @Test
  public void should_query_article_by_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
  }

  @Test
  public void should_query_article_by_favorite() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
    articleFavoriteRepository.save(articleFavorite);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(
            null, null, anotherUser.getUsername(), new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertEquals(articleData.getId(), article.getId());
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_query_article_by_tag() {
    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), user.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles("spring", null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList notag = queryService.findRecentArticles("notag", null, null, new Page(), user);
    Assertions.assertEquals(notag.getCount(), 0);
  }

  @Test
  public void should_show_following_if_user_followed_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }

  @Test
  public void should_get_user_feed() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
    Assertions.assertEquals(userFeed.getCount(), 0);

    ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
    Assertions.assertEquals(anotherUserFeed.getCount(), 1);
    ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }
}
