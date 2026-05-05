package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.campaign.ABTestVariant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ABTestVariantMapper {
  void insert(@Param("variant") ABTestVariant variant);

  List<ABTestVariant> findByCampaignId(@Param("campaignId") String campaignId);

  void deleteByCampaignId(@Param("campaignId") String campaignId);

  void update(@Param("variant") ABTestVariant variant);
}
