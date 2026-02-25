package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ProfileDatafetcher profileDatafetcher;
  private User authenticatedUser;

  @BeforeEach
  void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    authenticatedUser = new User("current@test.com", "currentuser", "pass", "", "");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            authenticatedUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_user_profile() {
    User user = new User("a@b.com", "testuser", "pass", "bio", "img");
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);

    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("testuser"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("img", result.getImage());
  }

  @Test
  void should_get_article_author() {
    ArticleData articleData = new ArticleData();
    articleData.setSlug("test-slug");
    articleData.setProfileData(new ProfileData("uid", "author1", "bio", "img", false));

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(article);

    ProfileData profileData = new ProfileData("uid", "author1", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("author1"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("author1", result.getUsername());
  }

  @Test
  void should_get_comment_author() {
    CommentData commentData = new CommentData();
    commentData.setId("c1");
    commentData.setProfileData(new ProfileData("uid", "commenter", "bio", "img", false));

    Map<String, CommentData> map = new HashMap<>();
    map.put("c1", commentData);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    Comment comment = Comment.newBuilder().id("c1").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);

    ProfileData profileData = new ProfileData("uid", "commenter", "bio", "img", false);
    when(profileQueryService.findByUsername(eq("commenter"), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("commenter", result.getUsername());
  }

  @Test
  void should_query_profile_by_username() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("targetuser");

    ProfileData profileData = new ProfileData("uid", "targetuser", "bio", "img", true);
    when(profileQueryService.findByUsername(eq("targetuser"), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result =
        profileDatafetcher.queryProfile("targetuser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
  }

  @Test
  void should_throw_when_profile_not_found() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
    when(profileQueryService.findByUsername(eq("nonexistent"), any()))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment));
  }
}
