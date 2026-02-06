package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.user.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PasswordResetTokenMapper {
  void insert(@Param("token") PasswordResetToken token);

  PasswordResetToken findByToken(@Param("token") String token);

  void update(@Param("token") PasswordResetToken token);

  void deleteByUserId(@Param("userId") String userId);
}
