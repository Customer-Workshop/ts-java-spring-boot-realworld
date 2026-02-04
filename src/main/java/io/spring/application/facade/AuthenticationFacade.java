package io.spring.application.facade;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

  public Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication == null
        || authentication.getPrincipal() == null) {
      return Optional.empty();
    }
    User currentUser = (User) authentication.getPrincipal();
    return Optional.of(currentUser);
  }

  public User requireCurrentUser() {
    return getCurrentUser()
        .orElseThrow(() -> new io.spring.graphql.exception.AuthenticationException());
  }
}
