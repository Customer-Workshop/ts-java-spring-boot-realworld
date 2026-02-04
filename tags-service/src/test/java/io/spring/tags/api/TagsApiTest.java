package io.spring.tags.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.tags.application.TagService;
import io.spring.tags.core.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
class TagsApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private TagService tagService;

  @Test
  void shouldGetAllTagNames() throws Exception {
    List<String> tagNames = Arrays.asList("java", "spring-boot", "microservices");
    when(tagService.getAllTagNames()).thenReturn(tagNames);

    mockMvc
        .perform(get("/api/tags"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags").isArray())
        .andExpect(jsonPath("$.tags[0]").value("java"))
        .andExpect(jsonPath("$.tags[1]").value("spring-boot"))
        .andExpect(jsonPath("$.tags[2]").value("microservices"));
  }

  @Test
  void shouldGetAllTagsWithDetails() throws Exception {
    List<Tag> tags = Arrays.asList(new Tag("tag-1", "java"), new Tag("tag-2", "spring-boot"));
    when(tagService.getAllTags()).thenReturn(tags);

    mockMvc
        .perform(get("/api/tags/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags").isArray())
        .andExpect(jsonPath("$.tags[0].id").value("tag-1"))
        .andExpect(jsonPath("$.tags[0].name").value("java"));
  }

  @Test
  void shouldGetTagById() throws Exception {
    Tag tag = new Tag("tag-1", "java");
    when(tagService.getTagById("tag-1")).thenReturn(Optional.of(tag));

    mockMvc
        .perform(get("/api/tags/tag-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tag.id").value("tag-1"))
        .andExpect(jsonPath("$.tag.name").value("java"));
  }

  @Test
  void shouldReturn404WhenTagNotFound() throws Exception {
    when(tagService.getTagById("nonexistent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/tags/nonexistent")).andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateTag() throws Exception {
    Tag tag = new Tag("tag-1", "java");
    when(tagService.createTag("java")).thenReturn(tag);

    CreateTagRequest request = new CreateTagRequest();
    request.setName("java");

    mockMvc
        .perform(
            post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tag.name").value("java"));
  }

  @Test
  void shouldDeleteTag() throws Exception {
    mockMvc.perform(delete("/api/tags/tag-1")).andExpect(status().isNoContent());
  }
}
