package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.campaign.CampaignAuditLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampaignAuditLogMapper {
  void insert(@Param("log") CampaignAuditLog log);

  List<CampaignAuditLog> findByCampaignId(@Param("campaignId") String campaignId);

  void deleteByCampaignId(@Param("campaignId") String campaignId);
}
