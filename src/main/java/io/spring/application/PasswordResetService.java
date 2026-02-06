package io.spring.application;

import io.spring.core.service.EmailService;
import io.spring.core.user.PasswordResetToken;
import io.spring.core.user.PasswordResetTokenRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
  private static final int DEFAULT_TOKEN_EXPIRY_MINUTES = 60;

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;
  private final int tokenExpiryMinutes;

  @Autowired
  public PasswordResetService(
      UserRepository userRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      EmailService emailService,
      PasswordEncoder passwordEncoder,
      @Value("${password.reset.token.expiry.minutes:60}") int tokenExpiryMinutes) {
    this.userRepository = userRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.emailService = emailService;
    this.passwordEncoder = passwordEncoder;
    this.tokenExpiryMinutes = tokenExpiryMinutes;
  }

  @Transactional
  public boolean requestPasswordReset(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
      return true;
    }

    User user = userOptional.get();
    passwordResetTokenRepository.deleteByUserId(user.getId());

    PasswordResetToken resetToken = new PasswordResetToken(user.getId(), tokenExpiryMinutes);
    passwordResetTokenRepository.save(resetToken);

    emailService.sendPasswordResetEmail(email, resetToken.getToken());
    return true;
  }

  @Transactional
  public boolean resetPassword(String token, String newPassword) {
    Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
    if (tokenOptional.isEmpty()) {
      return false;
    }

    PasswordResetToken resetToken = tokenOptional.get();
    if (!resetToken.isValid()) {
      return false;
    }

    Optional<User> userOptional = userRepository.findById(resetToken.getUserId());
    if (userOptional.isEmpty()) {
      return false;
    }

    User user = userOptional.get();
    String encodedPassword = passwordEncoder.encode(newPassword);
    user.update(null, null, encodedPassword, null, null);
    userRepository.save(user);

    resetToken.markAsUsed();
    passwordResetTokenRepository.update(resetToken);

    return true;
  }
}
