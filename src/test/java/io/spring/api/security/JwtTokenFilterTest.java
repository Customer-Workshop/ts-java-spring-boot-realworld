package io.spring.api.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

public class JwtTokenFilterTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtTokenFilter jwtTokenFilter;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    jwtTokenFilter = new JwtTokenFilter();
    ReflectionTestUtils.setField(jwtTokenFilter, "userRepository", userRepository);
    ReflectionTestUtils.setField(jwtTokenFilter, "jwtService", jwtService);
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_continue_filter_chain_without_auth_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
  }

  @Test
  public void should_continue_filter_chain_with_invalid_auth_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
  }

  @Test
  public void should_authenticate_user_with_valid_token() throws Exception {
    User user = new User("test@example.com", "testuser", "password", "", "");
    when(request.getHeader("Authorization")).thenReturn("Token valid-token");
    when(jwtService.getSubFromToken("valid-token")).thenReturn(Optional.of(user.getId()));
    when(userRepository.findById(user.getId())).thenReturn(Mono.just(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication(), notNullValue());
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(user));
  }

  @Test
  public void should_not_authenticate_with_invalid_token() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token invalid-token");
    when(jwtService.getSubFromToken("invalid-token")).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
  }

  @Test
  public void should_not_authenticate_when_user_not_found() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Token valid-token");
    when(jwtService.getSubFromToken("valid-token")).thenReturn(Optional.of("nonexistent-user-id"));
    when(userRepository.findById("nonexistent-user-id")).thenReturn(Mono.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
  }
}
