package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.campaign.Campaign;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampaignMapper {
  void insert(@Param("campaign") Campaign campaign);

  Campaign findById(@Param("id") String id);

  List<Campaign> findAll(@Param("includeArchived") boolean includeArchived);

  List<Campaign> findByStatus(
      @Param("status") String status, @Param("includeArchived") boolean includeArchived);

  void update(@Param("campaign") Campaign campaign);

  void delete(@Param("id") String id);
}
