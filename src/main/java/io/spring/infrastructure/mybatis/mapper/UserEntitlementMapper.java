package io.spring.infrastructure.mybatis.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEntitlementMapper {
  boolean hasEntitlement(@Param("userId") String userId, @Param("entitlement") String entitlement);

  List<String> findEntitlementsByUserId(@Param("userId") String userId);

  void insertEntitlement(
      @Param("id") String id,
      @Param("userId") String userId,
      @Param("entitlement") String entitlement);
}
