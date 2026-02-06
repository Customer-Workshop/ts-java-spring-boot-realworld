package io.spring.core.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetToken {
  private String id;
  private String token;
  private String userId;
  private Instant expiryDate;
  private boolean used;

  public PasswordResetToken(String userId, int expiryMinutes) {
    this.id = UUID.randomUUID().toString();
    this.token = UUID.randomUUID().toString();
    this.userId = userId;
    this.expiryDate = Instant.now().plusSeconds(expiryMinutes * 60L);
    this.used = false;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiryDate);
  }

  public boolean isValid() {
    return !isExpired() && !used;
  }

  public void markAsUsed() {
    this.used = true;
  }
}
