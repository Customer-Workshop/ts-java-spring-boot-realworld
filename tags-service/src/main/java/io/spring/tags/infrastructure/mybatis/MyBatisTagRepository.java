package io.spring.tags.infrastructure.mybatis;

import io.spring.tags.core.Tag;
import io.spring.tags.core.TagRepository;
import io.spring.tags.infrastructure.mybatis.mapper.TagMapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisTagRepository implements TagRepository {
  private TagMapper tagMapper;

  @Override
  public void save(Tag tag) {
    tagMapper.insert(tag);
  }

  @Override
  public Optional<Tag> findById(String id) {
    return Optional.ofNullable(tagMapper.findById(id));
  }

  @Override
  public Optional<Tag> findByName(String name) {
    return Optional.ofNullable(tagMapper.findByName(name));
  }

  @Override
  public List<Tag> findAll() {
    return tagMapper.findAll();
  }

  @Override
  public void delete(String id) {
    tagMapper.delete(id);
  }
}
