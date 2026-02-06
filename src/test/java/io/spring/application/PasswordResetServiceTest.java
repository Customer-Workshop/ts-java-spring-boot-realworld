package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.core.service.EmailService;
import io.spring.core.user.PasswordResetToken;
import io.spring.core.user.PasswordResetTokenRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordResetServiceTest {

  private UserRepository userRepository;
  private PasswordResetTokenRepository passwordResetTokenRepository;
  private EmailService emailService;
  private PasswordEncoder passwordEncoder;
  private PasswordResetService passwordResetService;

  @BeforeEach
  public void setUp() {
    userRepository = mock(UserRepository.class);
    passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
    emailService = mock(EmailService.class);
    passwordEncoder = new BCryptPasswordEncoder();
    passwordResetService =
        new PasswordResetService(
            userRepository, passwordResetTokenRepository, emailService, passwordEncoder, 60);
  }

  @Test
  public void should_request_password_reset_for_existing_user() {
    String email = "john@example.com";
    User user = new User(email, "john", "password", "", "");

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    boolean result = passwordResetService.requestPasswordReset(email);

    assertTrue(result);
    verify(passwordResetTokenRepository).deleteByUserId(eq(user.getId()));
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    verify(emailService).sendPasswordResetEmail(eq(email), any(String.class));
  }

  @Test
  public void should_return_true_for_non_existing_user() {
    String email = "nonexistent@example.com";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    boolean result = passwordResetService.requestPasswordReset(email);

    assertTrue(result);
    verify(passwordResetTokenRepository, never()).save(any());
    verify(emailService, never()).sendPasswordResetEmail(any(), any());
  }

  @Test
  public void should_reset_password_with_valid_token() {
    String newPassword = "newpassword123";
    User user = new User("john@example.com", "john", "oldpassword", "", "");
    PasswordResetToken resetToken = new PasswordResetToken(user.getId(), 60);
    String tokenString = resetToken.getToken();

    when(passwordResetTokenRepository.findByToken(eq(tokenString)))
        .thenReturn(Optional.of(resetToken));
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    boolean result = passwordResetService.resetPassword(tokenString, newPassword);

    assertTrue(result);
    verify(userRepository).save(any(User.class));
    verify(passwordResetTokenRepository).update(any(PasswordResetToken.class));
  }

  @Test
  public void should_fail_reset_with_invalid_token() {
    String tokenString = "invalid-token";
    String newPassword = "newpassword123";

    when(passwordResetTokenRepository.findByToken(eq(tokenString))).thenReturn(Optional.empty());

    boolean result = passwordResetService.resetPassword(tokenString, newPassword);

    assertFalse(result);
    verify(userRepository, never()).save(any());
  }

  @Test
  public void should_fail_reset_when_user_not_found() {
    String newPassword = "newpassword123";
    PasswordResetToken resetToken = new PasswordResetToken("nonexistent-user-id", 60);

    when(passwordResetTokenRepository.findByToken(eq(resetToken.getToken())))
        .thenReturn(Optional.of(resetToken));
    when(userRepository.findById(eq(resetToken.getUserId()))).thenReturn(Optional.empty());

    boolean result = passwordResetService.resetPassword(resetToken.getToken(), newPassword);

    assertFalse(result);
    verify(userRepository, never()).save(any());
  }
}
