package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * REST coverage for the Follow / Unfollow Profiles spec
 * (Cognition-Partner-Workshops/ts-java-spring-boot-realworld#187). Each test references the
 * acceptance criterion (AC) it covers.
 */
@WebMvcTest(ProfileApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ProfileApiTest extends TestWithCurrentUser {
  private User anotherUser;

  @Autowired private MockMvc mvc;

  @MockBean private ProfileQueryService profileQueryService;

  private String bio;
  private String image;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    bio = "my bio";
    image = "https://example.com/avatar.png";
    anotherUser = new User("username@test.com", "username", "123", bio, image);
    when(userRepository.findByUsername(eq(anotherUser.getUsername())))
        .thenReturn(Optional.of(anotherUser));
  }

  private ProfileData profile(boolean following) {
    return new ProfileData(anotherUser.getId(), anotherUser.getUsername(), bio, image, following);
  }

  // AC1: GET an existing user's profile returns 200 with username, bio and image.
  @Test
  public void should_return_profile_with_username_bio_and_image() {
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(null)))
        .thenReturn(Optional.of(profile(false)));

    RestAssuredMockMvc.when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(anotherUser.getUsername()))
        .body("profile.bio", equalTo(bio))
        .body("profile.image", equalTo(image));
  }

  // AC2: authenticated viewer who follows the target sees following=true.
  @Test
  public void should_show_following_true_for_authenticated_follower() {
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(true)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  // AC2: authenticated viewer who does not follow the target sees following=false.
  @Test
  public void should_show_following_false_for_authenticated_non_follower() {
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(false)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));
  }

  // AC3: anonymous viewer always sees following=false.
  @Test
  public void should_show_following_false_for_anonymous_viewer() {
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(null)))
        .thenReturn(Optional.of(profile(false)));

    RestAssuredMockMvc.when()
        .get("/profiles/{username}", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));

    verify(profileQueryService).findByUsername(eq(anotherUser.getUsername()), eq(null));
  }

  // AC4: requesting a non-existent username returns 404.
  @Test
  public void should_return_404_when_profile_not_found() {
    when(profileQueryService.findByUsername(eq("ghost"), any())).thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/profiles/{username}", "ghost")
        .prettyPeek()
        .then()
        .statusCode(404);
  }

  // AC5: authenticated follow of an existing user returns 200 and following=true.
  @Test
  public void should_follow_user_and_return_following_true() {
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(true)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));

    verify(userRepository).saveRelation(new FollowRelation(user.getId(), anotherUser.getId()));
  }

  // AC6: unauthenticated follow returns 401.
  @Test
  public void should_return_401_following_when_unauthenticated() {
    RestAssuredMockMvc.when()
        .post("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(401);

    verify(userRepository, never()).saveRelation(any());
  }

  // AC7: following a non-existent username returns 404.
  @Test
  public void should_return_404_following_nonexistent_user() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", "ghost")
        .prettyPeek()
        .then()
        .statusCode(404);

    verify(userRepository, never()).saveRelation(any());
  }

  // AC8: following a user already followed is idempotent -> 200, following=true.
  // (The "no duplicate relation" guarantee is enforced/verified at the repository tier.)
  @Test
  public void should_follow_idempotently_when_already_following() {
    when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId())))
        .thenReturn(Optional.of(new FollowRelation(user.getId(), anotherUser.getId())));
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(true)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(true));
  }

  // AC9: authenticated unfollow of a followed user returns 200 and following=false.
  @Test
  public void should_unfollow_user_and_return_following_false() {
    FollowRelation followRelation = new FollowRelation(user.getId(), anotherUser.getId());
    when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId())))
        .thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(false)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));

    verify(userRepository).removeRelation(eq(followRelation));
  }

  // AC10: unauthenticated unfollow returns 401.
  @Test
  public void should_return_401_unfollowing_when_unauthenticated() {
    RestAssuredMockMvc.when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(401);

    verify(userRepository, never()).removeRelation(any());
  }

  // AC11: unfollowing a user not currently followed is a no-op -> 200, following=false.
  @Test
  public void should_unfollow_noop_when_not_following() {
    when(userRepository.findRelation(eq(user.getId()), eq(anotherUser.getId())))
        .thenReturn(Optional.empty());
    when(profileQueryService.findByUsername(eq(anotherUser.getUsername()), eq(user)))
        .thenReturn(Optional.of(profile(false)));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/profiles/{username}/follow", anotherUser.getUsername())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("profile.following", equalTo(false));

    verify(userRepository, never()).removeRelation(any());
  }
}
