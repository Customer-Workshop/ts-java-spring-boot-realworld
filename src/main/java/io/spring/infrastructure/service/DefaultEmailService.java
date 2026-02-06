package io.spring.infrastructure.service;

import io.spring.core.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailService implements EmailService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEmailService.class);

  @Value("${app.base-url:http://localhost:3000}")
  private String baseUrl;

  @Override
  public void sendPasswordResetEmail(String to, String resetToken) {
    String resetLink = baseUrl + "/user/reset-password?token=" + resetToken;
    logger.info("Password reset email would be sent to: {}", to);
    logger.info("Reset link: {}", resetLink);
  }
}
