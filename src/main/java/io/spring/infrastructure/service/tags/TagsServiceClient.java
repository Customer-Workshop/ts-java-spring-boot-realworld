package io.spring.infrastructure.service.tags;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TagsServiceClient {
  private final RestTemplate restTemplate;
  private final String tagsServiceUrl;
  private final boolean enabled;

  public TagsServiceClient(
      @Value("${tags.service.url:}") String tagsServiceUrl,
      @Value("${tags.service.enabled:false}") boolean enabled) {
    this.restTemplate = new RestTemplate();
    this.tagsServiceUrl = tagsServiceUrl;
    this.enabled = enabled && tagsServiceUrl != null && !tagsServiceUrl.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public List<String> getAllTags() {
    if (!enabled) {
      return Collections.emptyList();
    }
    try {
      Map<String, Object> response =
          restTemplate.getForObject(tagsServiceUrl + "/api/tags", Map.class);
      if (response != null && response.containsKey("tags")) {
        return (List<String>) response.get("tags");
      }
      return Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createTag(String name) {
    if (!enabled) {
      return Collections.emptyMap();
    }
    try {
      Map<String, String> request = Collections.singletonMap("name", name);
      return restTemplate.postForObject(tagsServiceUrl + "/api/tags", request, Map.class);
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createTags(List<String> names) {
    if (!enabled) {
      return Collections.emptyMap();
    }
    try {
      Map<String, List<String>> request = Collections.singletonMap("names", names);
      return restTemplate.postForObject(tagsServiceUrl + "/api/tags/batch", request, Map.class);
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  public boolean isEnabled() {
    return enabled;
  }
}
