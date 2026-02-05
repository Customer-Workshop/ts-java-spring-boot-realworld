package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.comment.Comment;

public interface CommentMapper {
  void insert(Comment comment);

  Comment findById(String articleId, String id);

  void delete(String id);
}
