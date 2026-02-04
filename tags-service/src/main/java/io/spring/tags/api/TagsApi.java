package io.spring.tags.api;

import io.spring.tags.application.TagService;
import io.spring.tags.core.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/tags")
@AllArgsConstructor
public class TagsApi {
  private TagService tagService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagService.getAllTagNames());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/all")
  public ResponseEntity<Map<String, Object>> getAllTagsWithDetails() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagService.getAllTags());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getTagById(@PathVariable String id) {
    return tagService
        .getTagById(id)
        .map(
            tag -> {
              Map<String, Object> response = new HashMap<>();
              response.put("tag", tag);
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<Map<String, Object>> getTagByName(@PathVariable String name) {
    return tagService
        .getTagByName(name)
        .map(
            tag -> {
              Map<String, Object> response = new HashMap<>();
              response.put("tag", tag);
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createTag(@RequestBody CreateTagRequest request) {
    Tag tag = tagService.createTag(request.getName());
    Map<String, Object> response = new HashMap<>();
    response.put("tag", tag);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/batch")
  public ResponseEntity<Map<String, Object>> createTags(@RequestBody CreateTagsRequest request) {
    List<Tag> tags = tagService.createTags(request.getNames());
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tags);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTag(@PathVariable String id) {
    tagService.deleteTag(id);
    return ResponseEntity.noContent().build();
  }
}
