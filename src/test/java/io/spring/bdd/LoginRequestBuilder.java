package io.spring.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Utility for constructing JSON payloads for the POST /api/users/login endpoint. */
public final class LoginRequestBuilder {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private LoginRequestBuilder() {}

  /**
   * Build a full login request body: {@code {"user":{"email":"...","password":"..."}}}.
   *
   * @param email the email value (included only when non-null)
   * @param password the password value (included only when non-null)
   * @return the JSON string
   */
  public static String buildLoginJson(String email, String password) {
    ObjectNode root = MAPPER.createObjectNode();
    ObjectNode user = MAPPER.createObjectNode();

    if (email != null) {
      user.put("email", email);
    }
    if (password != null) {
      user.put("password", password);
    }

    root.set("user", user);
    return root.toString();
  }
}
