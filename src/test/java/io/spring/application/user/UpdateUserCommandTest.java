package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  public void should_create_update_user_command() {
    User user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    UpdateUserParam param = new UpdateUserParam("new@example.com", "newpassword", "newuser", "new bio", "new-image.jpg");

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertThat(command.getTargetUser(), notNullValue());
    assertThat(command.getParam(), notNullValue());
    assertThat(command.getTargetUser().getEmail(), is("test@example.com"));
    assertThat(command.getParam().getEmail(), is("new@example.com"));
  }
}
