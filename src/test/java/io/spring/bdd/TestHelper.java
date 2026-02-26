package io.spring.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Shared utility class for Cucumber BDD integration tests.
 *
 * <p>Provides convenience methods for:
 *
 * <ul>
 *   <li>HTTP requests (GET, POST, PUT, DELETE) against the running application
 *   <li>Building JSON payloads for user registration, login, and profile updates
 *   <li>Storing and retrieving JWT tokens for authenticated requests
 *   <li>Validating JSON response structure (field presence, types, nested wrappers)
 * </ul>
 *
 * <p>This class is Spring-managed ({@link Component}) so it can be injected into step definition
 * classes. The embedded server port is automatically resolved via {@link LocalServerPort}.
 */
@Component
public class TestHelper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @LocalServerPort private int port;

  private final TestRestTemplate restTemplate = new TestRestTemplate();

  private String currentToken;

  // ---------------------------------------------------------------------------
  // Base URL
  // ---------------------------------------------------------------------------

  /** Returns the base URL of the running application (e.g. {@code http://localhost:8080}). */
  public String baseUrl() {
    return "http://localhost:" + port;
  }

  // ---------------------------------------------------------------------------
  // JWT token management
  // ---------------------------------------------------------------------------

  /** Stores a JWT token for use in subsequent authenticated requests. */
  public void setToken(String token) {
    this.currentToken = token;
  }

  /** Returns the currently stored JWT token, or {@code null} if none has been set. */
  public String getToken() {
    return currentToken;
  }

  /** Clears any stored JWT token. */
  public void clearToken() {
    this.currentToken = null;
  }

  // ---------------------------------------------------------------------------
  // HTTP request helpers
  // ---------------------------------------------------------------------------

  /**
   * Sends an HTTP GET request to the given path.
   *
   * @param path relative path (e.g. {@code /api/articles})
   * @return the response entity with the body as a {@code String}
   */
  public ResponseEntity<String> get(String path) {
    return exchange(path, HttpMethod.GET, null);
  }

  /**
   * Sends an HTTP POST request with a JSON body.
   *
   * @param path relative path
   * @param jsonBody the JSON string body
   * @return the response entity
   */
  public ResponseEntity<String> post(String path, String jsonBody) {
    return exchange(path, HttpMethod.POST, jsonBody);
  }

  /**
   * Sends an HTTP PUT request with a JSON body.
   *
   * @param path relative path
   * @param jsonBody the JSON string body
   * @return the response entity
   */
  public ResponseEntity<String> put(String path, String jsonBody) {
    return exchange(path, HttpMethod.PUT, jsonBody);
  }

  /**
   * Sends an HTTP DELETE request to the given path.
   *
   * @param path relative path
   * @return the response entity
   */
  public ResponseEntity<String> delete(String path) {
    return exchange(path, HttpMethod.DELETE, null);
  }

  private ResponseEntity<String> exchange(String path, HttpMethod method, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    if (currentToken != null) {
      headers.set("Authorization", "Token " + currentToken);
    }

    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    return restTemplate.exchange(baseUrl() + path, method, entity, String.class);
  }

  // ---------------------------------------------------------------------------
  // Payload builders
  // ---------------------------------------------------------------------------

  /**
   * Builds a JSON payload for user registration.
   *
   * @param username desired username
   * @param email user email address
   * @param password user password
   * @return a JSON string in the format {@code {"user":
   *     {"username":...,"email":...,"password":...}}}
   */
  public String buildRegisterPayload(String username, String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("username", username);
    user.put("email", email);
    user.put("password", password);
    return wrapInKey("user", user);
  }

  /**
   * Builds a JSON payload for user login.
   *
   * @param email user email address
   * @param password user password
   * @return a JSON string in the format {@code {"user": {"email":...,"password":...}}}
   */
  public String buildLoginPayload(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);
    return wrapInKey("user", user);
  }

  /**
   * Builds a JSON payload for updating the current user profile.
   *
   * @param fields a map of fields to update (e.g. {@code bio}, {@code image})
   * @return a JSON string wrapped under the {@code "user"} key
   */
  public String buildUpdateUserPayload(Map<String, Object> fields) {
    return wrapInKey("user", fields);
  }

  /**
   * Builds a JSON payload for creating an article.
   *
   * @param title article title
   * @param description article description
   * @param body article body text
   * @param tagList list of tag strings
   * @return a JSON string wrapped under the {@code "article"} key
   */
  public String buildCreateArticlePayload(
      String title, String description, String body, java.util.List<String> tagList) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", tagList);
    return wrapInKey("article", article);
  }

  /**
   * Builds a JSON payload for adding a comment to an article.
   *
   * @param body the comment body text
   * @return a JSON string wrapped under the {@code "comment"} key
   */
  public String buildCommentPayload(String body) {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", body);
    return wrapInKey("comment", comment);
  }

  private String wrapInKey(String key, Map<String, Object> value) {
    try {
      Map<String, Object> wrapper = new HashMap<>();
      wrapper.put(key, value);
      return OBJECT_MAPPER.writeValueAsString(wrapper);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize JSON payload", e);
    }
  }

  // ---------------------------------------------------------------------------
  // JSON validation helpers
  // ---------------------------------------------------------------------------

  /**
   * Parses a JSON string into a {@link JsonNode}.
   *
   * @param json the raw JSON string
   * @return the parsed {@link JsonNode}
   * @throws IllegalStateException if parsing fails
   */
  public JsonNode parseJson(String json) {
    try {
      return OBJECT_MAPPER.readTree(json);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException("Failed to parse JSON: " + json, e);
    }
  }

  /**
   * Asserts that the given {@link JsonNode} contains a field with the specified name.
   *
   * @param node the JSON node to inspect
   * @param fieldName the expected field name
   * @return {@code true} if the field exists
   */
  public boolean hasField(JsonNode node, String fieldName) {
    return node.has(fieldName);
  }

  /**
   * Asserts that a field in the given {@link JsonNode} is of the expected JSON type.
   *
   * <p>Supported type names (case-insensitive): {@code string}, {@code number}, {@code boolean},
   * {@code array}, {@code object}, {@code null}.
   *
   * @param node the JSON node containing the field
   * @param fieldName the field to check
   * @param expectedType the expected type name
   * @return {@code true} if the field exists and matches the expected type
   */
  public boolean fieldHasType(JsonNode node, String fieldName, String expectedType) {
    if (!node.has(fieldName)) {
      return false;
    }
    JsonNode field = node.get(fieldName);
    switch (expectedType.toLowerCase()) {
      case "string":
        return field.isTextual();
      case "number":
        return field.isNumber();
      case "boolean":
        return field.isBoolean();
      case "array":
        return field.isArray();
      case "object":
        return field.isObject();
      case "null":
        return field.isNull();
      default:
        throw new IllegalArgumentException("Unknown JSON type: " + expectedType);
    }
  }

  /**
   * Validates that the JSON response is wrapped under the specified top-level key and that the
   * inner object contains all expected field names.
   *
   * @param json the raw JSON response string
   * @param wrapperKey the expected top-level wrapper key (e.g. {@code "user"}, {@code "article"})
   * @param expectedFields the field names expected inside the wrapper object
   * @return {@code true} if the wrapper key exists and all fields are present
   */
  public boolean validateWrappedResponse(
      String json, String wrapperKey, java.util.List<String> expectedFields) {
    JsonNode root = parseJson(json);
    if (!root.has(wrapperKey)) {
      return false;
    }
    JsonNode inner = root.get(wrapperKey);
    for (String field : expectedFields) {
      if (!inner.has(field)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns all field names present in the given {@link JsonNode}.
   *
   * @param node the JSON node to inspect
   * @return a list of field names
   */
  public java.util.List<String> getFieldNames(JsonNode node) {
    java.util.List<String> names = new java.util.ArrayList<>();
    Iterator<String> iterator = node.fieldNames();
    while (iterator.hasNext()) {
      names.add(iterator.next());
    }
    return names;
  }
}
