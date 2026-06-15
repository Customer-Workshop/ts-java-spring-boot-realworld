package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtilTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_return_current_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "img");
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }

  @Test
  void should_return_empty_when_anonymous() {
    AnonymousAuthenticationToken anon =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anon);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isEmpty());
  }

  @Test
  void should_return_empty_when_principal_is_null() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isEmpty());
  }
}
