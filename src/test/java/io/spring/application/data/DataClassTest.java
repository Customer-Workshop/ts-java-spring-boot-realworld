package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassTest {

  @Test
  void should_create_article_data_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", true);
    ArticleData data =
        new ArticleData(
            "id1",
            "slug",
            "title",
            "desc",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("id1", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(Arrays.asList("java"), data.getTagList());
    assertEquals(profile, data.getProfileData());
    assertNotNull(data.getCursor());
    assertNotNull(data.toString());
    assertNotNull(data.hashCode());
  }

  @Test
  void should_create_article_data_with_no_args_and_setters() {
    ArticleData data = new ArticleData();
    DateTime now = new DateTime();
    data.setId("id2");
    data.setSlug("slug2");
    data.setTitle("title2");
    data.setDescription("desc2");
    data.setBody("body2");
    data.setFavorited(false);
    data.setFavoritesCount(3);
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("spring"));
    data.setProfileData(new ProfileData("pid", "u", "b", "i", false));

    assertEquals("id2", data.getId());
    assertEquals("slug2", data.getSlug());
    assertEquals("title2", data.getTitle());
    assertEquals("desc2", data.getDescription());
    assertEquals("body2", data.getBody());
    assertFalse(data.isFavorited());
    assertEquals(3, data.getFavoritesCount());
  }

  @Test
  void should_test_article_data_equals() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id", "slug", "t", "d", "b", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id", "slug", "t", "d", "b", false, 0, now, now, null, null);
    ArticleData data3 =
        new ArticleData("other", "slug2", "t2", "d2", "b2", true, 1, now, now, null, null);

    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1, data1);
    assertNotEquals(data1, null);
    assertNotEquals(data1, "string");
  }

  @Test
  void should_create_comment_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("pid", "user", "bio", "img", false);
    CommentData data = new CommentData("c1", "body", "artId", now, now, profile);

    assertEquals("c1", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("artId", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
    assertNotNull(data.getCursor());
    assertNotNull(data.toString());
    assertNotNull(data.hashCode());
  }

  @Test
  void should_create_comment_data_with_setters() {
    CommentData data = new CommentData();
    DateTime now = new DateTime();
    data.setId("c2");
    data.setBody("new body");
    data.setArticleId("art2");
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(null);

    assertEquals("c2", data.getId());
    assertEquals("new body", data.getBody());
    assertEquals("art2", data.getArticleId());
  }

  @Test
  void should_test_comment_data_equals() {
    DateTime now = new DateTime();
    CommentData data1 = new CommentData("c1", "body", "art", now, now, null);
    CommentData data2 = new CommentData("c1", "body", "art", now, now, null);
    assertEquals(data1, data2);
  }

  @Test
  void should_create_profile_data() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
    assertNotNull(data.toString());
    assertNotNull(data.hashCode());
  }

  @Test
  void should_create_profile_data_with_setters() {
    ProfileData data = new ProfileData();
    data.setId("id2");
    data.setUsername("user2");
    data.setBio("bio2");
    data.setImage("img2");
    data.setFollowing(false);

    assertEquals("id2", data.getId());
    assertEquals("user2", data.getUsername());
    assertFalse(data.isFollowing());
  }

  @Test
  void should_test_profile_data_equals() {
    ProfileData data1 = new ProfileData("id", "user", "bio", "img", true);
    ProfileData data2 = new ProfileData("id", "user", "bio", "img", true);
    ProfileData data3 = new ProfileData("other", "user2", "bio2", "img2", false);
    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  void should_create_user_data() {
    UserData data = new UserData("id", "email@test.com", "user", "bio", "img");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertNotNull(data.toString());
    assertNotNull(data.hashCode());
  }

  @Test
  void should_create_user_data_with_setters() {
    UserData data = new UserData();
    data.setId("id2");
    data.setEmail("e2@test.com");
    data.setUsername("u2");
    data.setBio("b2");
    data.setImage("i2");
    assertEquals("id2", data.getId());
  }

  @Test
  void should_test_user_data_equals() {
    UserData data1 = new UserData("id", "e", "u", "b", "i");
    UserData data2 = new UserData("id", "e", "u", "b", "i");
    assertEquals(data1, data2);
  }

  @Test
  void should_create_user_with_token() {
    UserData userData = new UserData("id", "email@test.com", "user", "bio", "img");
    UserWithToken uwt = new UserWithToken(userData, "mytoken");

    assertEquals("email@test.com", uwt.getEmail());
    assertEquals("user", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("img", uwt.getImage());
    assertEquals("mytoken", uwt.getToken());
  }

  @Test
  void should_create_article_data_list() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "t", "d", "b", false, 0, now, now, null, null);
    List<ArticleData> articles = Arrays.asList(article);
    ArticleDataList list = new ArticleDataList(articles, 1);

    assertEquals(1, list.getCount());
    assertEquals(articles, list.getArticleDatas());
  }

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("artId", 10);

    assertEquals("artId", afc.getId());
    assertEquals(10, afc.getCount());
    assertNotNull(afc.toString());
    assertNotNull(afc.hashCode());

    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("artId", 10);
    assertEquals(afc, afc2);
  }
}
