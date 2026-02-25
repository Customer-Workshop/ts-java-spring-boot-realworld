package io.spring.cucumber;

import java.util.HashMap;
import java.util.Map;

/** Utility class for constructing JSON request payloads for the RealWorld API. */
public final class PayloadBuilder {

  private PayloadBuilder() {}

  /**
   * Build a registration payload: { "user": { "email": ..., "username": ..., "password": ... } }
   */
  public static Map<String, Object> registrationPayload(
      String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);
    Map<String, Object> wrapper = new HashMap<>();
    wrapper.put("user", user);
    return wrapper;
  }

  /** Build a registration payload with a specific field omitted. */
  public static Map<String, Object> registrationPayloadWithout(String fieldToOmit) {
    Map<String, Object> user = new HashMap<>();
    if (!"email".equals(fieldToOmit)) {
      user.put("email", "test@example.com");
    }
    if (!"username".equals(fieldToOmit)) {
      user.put("username", "testuser");
    }
    if (!"password".equals(fieldToOmit)) {
      user.put("password", "password123");
    }
    Map<String, Object> wrapper = new HashMap<>();
    wrapper.put("user", user);
    return wrapper;
  }

  /** Build a login payload: { "user": { "email": ..., "password": ... } } */
  public static Map<String, Object> loginPayload(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);
    Map<String, Object> wrapper = new HashMap<>();
    wrapper.put("user", user);
    return wrapper;
  }

  /** Build an update user payload: { "user": { ... } } with only specified fields. */
  public static Map<String, Object> updateUserPayload(Map<String, String> fields) {
    Map<String, Object> user = new HashMap<>(fields);
    Map<String, Object> wrapper = new HashMap<>();
    wrapper.put("user", user);
    return wrapper;
  }

  /** Generate a string of a given length by repeating a character. */
  public static String generateString(int length, char ch) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ch);
    }
    return sb.toString();
  }
}
