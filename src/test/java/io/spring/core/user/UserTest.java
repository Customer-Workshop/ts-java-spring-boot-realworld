package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@example.com", "testuser", "password123", "bio text", "image.jpg");

    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password123"));
    assertThat(user.getBio(), is("bio text"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void should_generate_unique_id() {
    User user1 = new User("test1@example.com", "user1", "pass1", "", "");
    User user2 = new User("test2@example.com", "user2", "pass2", "", "");

    assertThat(user1.getId(), not(user2.getId()));
  }

  @Test
  public void should_update_email() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    user.update("new@example.com", null, null, null, null);

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("testuser"));
  }

  @Test
  public void should_update_username() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    user.update(null, "newuser", null, null, null);

    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getEmail(), is("test@example.com"));
  }

  @Test
  public void should_update_password() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    user.update(null, null, "newpass", null, null);

    assertThat(user.getPassword(), is("newpass"));
  }

  @Test
  public void should_update_bio() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    user.update(null, null, null, "new bio", null);

    assertThat(user.getBio(), is("new bio"));
  }

  @Test
  public void should_update_image() {
    User user = new User("test@example.com", "testuser", "password", "bio", "old.jpg");
    user.update(null, null, null, null, "new.jpg");

    assertThat(user.getImage(), is("new.jpg"));
  }

  @Test
  public void should_update_all_fields() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "old.jpg");
    user.update("new@example.com", "newuser", "newpass", "new bio", "new.jpg");

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new.jpg"));
  }

  @Test
  public void should_not_update_with_empty_values() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image.jpg");
    user.update("", "", "", "", "");

    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
  }

  @Test
  public void should_be_equal_by_id() {
    User user1 = new User("test@example.com", "testuser", "password", "bio", "image");
    User user2 = new User("other@example.com", "otheruser", "otherpass", "other bio", "other.jpg");

    assertThat(user1.equals(user2), is(false));
    assertThat(user1.equals(user1), is(true));
  }

  @Test
  public void should_have_consistent_hashcode() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    int hashCode1 = user.hashCode();
    int hashCode2 = user.hashCode();

    assertThat(hashCode1, is(hashCode2));
  }
}
