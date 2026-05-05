package io.spring.infrastructure.mybatis.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampaignTagMapper {
  void insert(@Param("id") String id, @Param("campaignId") String campaignId, @Param("tag") String tag);

  List<String> findByCampaignId(@Param("campaignId") String campaignId);

  void deleteByCampaignId(@Param("campaignId") String campaignId);

  List<String> findAllDistinctTags();
}
