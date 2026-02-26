package io.spring.cucumber.steps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Helper for parsing HTTP response bodies. Uses a plain ObjectMapper without Spring's
 * UNWRAP_ROOT_VALUE setting, which would cause readTree() to fail on wrapped JSON payloads.
 */
public final class ResponseHelper {

  private static final ObjectMapper READER =
      new ObjectMapper().disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

  private ResponseHelper() {}

  /** Parse the response body of an MvcResult into a JsonNode tree. */
  public static JsonNode parseResponse(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    return READER.readTree(body);
  }

  /** Convenience: parse and return the nested "article" object. */
  public static JsonNode articleFromResponse(MvcResult result) throws Exception {
    return parseResponse(result).get("article");
  }

  /** Convenience: parse and return the nested "comment" object. */
  public static JsonNode commentFromResponse(MvcResult result) throws Exception {
    return parseResponse(result).get("comment");
  }
}
