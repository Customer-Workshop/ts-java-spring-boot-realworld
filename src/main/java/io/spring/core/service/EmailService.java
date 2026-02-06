package io.spring.core.service;

public interface EmailService {
  void sendPasswordResetEmail(String to, String resetToken);
}
