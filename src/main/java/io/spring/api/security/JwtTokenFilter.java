package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {
  private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> tokenOptional = getTokenString(request.getHeader(header));

    if (tokenOptional.isPresent()) {
      String token = tokenOptional.get();
      Optional<String> userIdOptional = jwtService.getSubFromToken(token);

      if (userIdOptional.isEmpty()) {
        logger.warn(
            "JWT token validation failed for request to {} - token was present but could not be validated",
            request.getRequestURI());
      } else {
        String userId = userIdOptional.get();
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          Optional<io.spring.core.user.User> userOptional = userRepository.findById(userId);

          if (userOptional.isEmpty()) {
            logger.warn(
                "User not found in database for user ID extracted from JWT token: {}", userId);
          } else {
            io.spring.core.user.User user = userOptional.get();
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.debug(
                "Successfully authenticated user '{}' for request to {}",
                user.getUsername(),
                request.getRequestURI());
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
