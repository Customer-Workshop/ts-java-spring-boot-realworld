package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_user_data() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio text", "image.jpg");

    assertThat(userData.getId(), is("user-1"));
    assertThat(userData.getEmail(), is("test@example.com"));
    assertThat(userData.getUsername(), is("testuser"));
    assertThat(userData.getBio(), is("bio text"));
    assertThat(userData.getImage(), is("image.jpg"));
  }

  @Test
  public void should_be_equal_with_same_values() {
    UserData userData1 = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    UserData userData2 = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");

    assertThat(userData1.equals(userData2), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    UserData userData = new UserData("user-1", "test@example.com", "testuser", "bio", "image.jpg");
    int hashCode1 = userData.hashCode();
    int hashCode2 = userData.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }

  @Test
  public void should_set_values() {
    UserData userData = new UserData();
    userData.setId("user-1");
    userData.setEmail("test@example.com");
    userData.setUsername("testuser");
    userData.setBio("bio");
    userData.setImage("image.jpg");

    assertThat(userData.getId(), is("user-1"));
    assertThat(userData.getEmail(), is("test@example.com"));
    assertThat(userData.getUsername(), is("testuser"));
    assertThat(userData.getBio(), is("bio"));
    assertThat(userData.getImage(), is("image.jpg"));
  }
}
