package io.spring.tags.application;

import io.spring.tags.core.Tag;
import io.spring.tags.core.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagService {
  private TagRepository tagRepository;

  public List<String> getAllTagNames() {
    return tagRepository.findAll().stream().map(Tag::getName).collect(Collectors.toList());
  }

  public List<Tag> getAllTags() {
    return tagRepository.findAll();
  }

  public Optional<Tag> getTagById(String id) {
    return tagRepository.findById(id);
  }

  public Optional<Tag> getTagByName(String name) {
    return tagRepository.findByName(name);
  }

  public Tag createTag(String name) {
    Optional<Tag> existingTag = tagRepository.findByName(name);
    if (existingTag.isPresent()) {
      return existingTag.get();
    }
    Tag tag = new Tag(name);
    tagRepository.save(tag);
    return tag;
  }

  public List<Tag> createTags(List<String> names) {
    return names.stream().map(this::createTag).collect(Collectors.toList());
  }

  public void deleteTag(String id) {
    tagRepository.delete(id);
  }
}
