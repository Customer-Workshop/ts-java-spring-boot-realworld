package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileDatafetcherTest {

  private ProfileQueryService profileQueryService;
  private ProfileDatafetcher profileDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    profileQueryService = mock(ProfileQueryService.class);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(user);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", true);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dfe);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
    assertTrue(result.getFollowing());
  }

  @Test
  void should_get_article_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData =
        new ArticleData(
            "id",
            "test-slug",
            "Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            null,
            new ProfileData("uid", "authoruser", "bio", "img", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    ProfileData profileData = new ProfileData("uid", "authoruser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("authoruser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dfe);

    assertNotNull(result);
    assertEquals("authoruser", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Comment comment = Comment.newBuilder().id("c1").body("text").build();
    when(dfe.getSource()).thenReturn(comment);

    CommentData commentData =
        new CommentData(
            "c1",
            "text",
            "artId",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "commenter", "bio", "img", false));
    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);
    when(dfe.getLocalContext()).thenReturn(map);

    ProfileData profileData = new ProfileData("uid", "commenter", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("commenter"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dfe);

    assertNotNull(result);
    assertEquals("commenter", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getArgument("username")).thenReturn("someuser");

    ProfileData profileData = new ProfileData("uid", "someuser", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("someuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("someuser", dfe);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("someuser", result.getProfile().getUsername());
    assertTrue(result.getProfile().getFollowing());
  }
}
