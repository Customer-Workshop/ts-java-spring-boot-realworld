package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;

public interface UserMapper {
  void insert(User user);

  User findByUsername(String username);

  User findByEmail(String email);

  User findById(String id);

  void update(User user);

  FollowRelation findRelation(String userId, String targetId);

  void saveRelation(FollowRelation followRelation);

  void deleteRelation(FollowRelation followRelation);
}
