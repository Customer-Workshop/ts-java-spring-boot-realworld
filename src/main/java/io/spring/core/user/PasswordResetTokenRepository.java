package io.spring.core.user;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository {
  void save(PasswordResetToken token);

  Optional<PasswordResetToken> findByToken(String token);

  void update(PasswordResetToken token);

  void deleteByUserId(String userId);
}
