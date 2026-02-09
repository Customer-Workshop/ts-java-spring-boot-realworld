package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_profile_data() {
    ProfileData profile = new ProfileData("user-1", "testuser", "bio text", "image.jpg", true);

    assertThat(profile.getId(), is("user-1"));
    assertThat(profile.getUsername(), is("testuser"));
    assertThat(profile.getBio(), is("bio text"));
    assertThat(profile.getImage(), is("image.jpg"));
    assertThat(profile.isFollowing(), is(true));
  }

  @Test
  public void should_set_following() {
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    profile.setFollowing(true);

    assertThat(profile.isFollowing(), is(true));
  }

  @Test
  public void should_be_equal_with_same_values() {
    ProfileData profile1 = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    ProfileData profile2 = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);

    assertThat(profile1.equals(profile2), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    ProfileData profile = new ProfileData("user-1", "testuser", "bio", "image.jpg", false);
    int hashCode1 = profile.hashCode();
    int hashCode2 = profile.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
