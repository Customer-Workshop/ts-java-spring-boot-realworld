package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.campaign.CampaignDecision;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampaignDecisionMapper {
  void insert(@Param("decision") CampaignDecision decision);

  List<CampaignDecision> findByCampaignId(@Param("campaignId") String campaignId);

  int countByCampaignId(@Param("campaignId") String campaignId);

  int countByCampaignIdAndDecision(
      @Param("campaignId") String campaignId, @Param("decision") String decision);

  List<Map<String, Object>> countAllByDecisionForNonArchived();

  void deleteByCampaignId(@Param("campaignId") String campaignId);
}
