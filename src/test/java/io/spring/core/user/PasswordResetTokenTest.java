package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PasswordResetTokenTest {

  @Test
  public void should_create_valid_token() {
    String userId = "user-123";
    int expiryMinutes = 60;

    PasswordResetToken token = new PasswordResetToken(userId, expiryMinutes);

    assertNotNull(token.getId());
    assertNotNull(token.getToken());
    assertEquals(userId, token.getUserId());
    assertNotNull(token.getExpiryDate());
    assertFalse(token.isUsed());
    assertTrue(token.isValid());
    assertFalse(token.isExpired());
  }

  @Test
  public void should_mark_token_as_used() {
    PasswordResetToken token = new PasswordResetToken("user-123", 60);

    assertTrue(token.isValid());

    token.markAsUsed();

    assertTrue(token.isUsed());
    assertFalse(token.isValid());
  }

  @Test
  public void should_be_expired_when_expiry_time_passed() {
    PasswordResetToken token = new PasswordResetToken("user-123", 0);

    assertTrue(token.isExpired());
    assertFalse(token.isValid());
  }
}
