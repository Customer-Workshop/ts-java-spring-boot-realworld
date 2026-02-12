package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@email.com", "testuser", "password", "bio", "image.jpg");
    assertThat(user.getEmail(), is("test@email.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image.jpg"));
    assertThat(user.getId(), notNullValue());
  }

  @Test
  public void should_generate_unique_ids() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");
    assertThat(user1.getId(), not(user2.getId()));
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@email.com", "user", "pass", "bio", "img");
    user.update("new@email.com", "", "", "", "");
    assertThat(user.getEmail(), is("new@email.com"));
    assertThat(user.getUsername(), is("user"));
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("a@b.com", "oldname", "pass", "bio", "img");
    user.update("", "newname", "", "", "");
    assertThat(user.getUsername(), is("newname"));
    assertThat(user.getEmail(), is("a@b.com"));
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("a@b.com", "user", "oldpass", "", "");
    user.update("", "", "newpass", "", "");
    assertThat(user.getPassword(), is("newpass"));
  }

  @Test
  public void should_update_bio_and_image() {
    User user = new User("a@b.com", "user", "pass", "", "");
    user.update("", "", "", "new bio", "new-image.jpg");
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new-image.jpg"));
  }

  @Test
  public void should_not_update_fields_when_null() {
    User user = new User("a@b.com", "user", "pass", "bio", "img");
    user.update(null, null, null, null, null);
    assertThat(user.getEmail(), is("a@b.com"));
    assertThat(user.getUsername(), is("user"));
    assertThat(user.getPassword(), is("pass"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("img"));
  }

  @Test
  public void should_update_all_fields_at_once() {
    User user = new User("a@b.com", "user", "pass", "bio", "img");
    user.update("new@b.com", "newuser", "newpass", "new bio", "new img");
    assertThat(user.getEmail(), is("new@b.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new img"));
  }

  @Test
  public void should_have_equality_based_on_id() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");
    assertThat(user1.equals(user2), is(false));
    assertThat(user1.equals(user1), is(true));
  }
}
