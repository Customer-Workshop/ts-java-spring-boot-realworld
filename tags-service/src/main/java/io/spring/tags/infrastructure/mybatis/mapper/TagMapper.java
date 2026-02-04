package io.spring.tags.infrastructure.mybatis.mapper;

import io.spring.tags.core.Tag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagMapper {
  void insert(@Param("tag") Tag tag);

  Tag findById(@Param("id") String id);

  Tag findByName(@Param("name") String name);

  List<Tag> findAll();

  void delete(@Param("id") String id);
}
