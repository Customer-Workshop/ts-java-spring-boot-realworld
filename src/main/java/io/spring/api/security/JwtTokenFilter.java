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
    String requestPath = request.getRequestURI();
    Optional<String> tokenOptional = getTokenString(request.getHeader(header), requestPath);

    if (tokenOptional.isPresent()) {
      String token = tokenOptional.get();
      Optional<String> userIdOptional = jwtService.getSubFromToken(token);

      if (userIdOptional.isPresent()) {
        String userId = userIdOptional.get();
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          Optional<io.spring.core.user.User> userOptional = userRepository.findById(userId);

          if (userOptional.isPresent()) {
            io.spring.core.user.User user = userOptional.get();
            logger.debug(
                "User successfully authenticated: userId={}, username={}, path={}",
                userId,
                user.getUsername(),
                requestPath);
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
          } else {
            logger.warn(
                "JWT token valid but user not found in database: userId={}, path={}",
                userId,
                requestPath);
          }
        }
      } else {
        logger.debug("JWT token validation failed for path={}", requestPath);
      }
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenString(String header, String requestPath) {
    if (header == null) {
      logger.debug("Authorization header is missing for path={}", requestPath);
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        logger.warn(
            "Authorization header is malformed (expected 'Token <jwt>' format): header={}, path={}",
            header,
            requestPath);
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
