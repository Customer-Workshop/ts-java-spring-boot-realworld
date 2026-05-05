package io.spring.infrastructure.repository;

import io.spring.core.campaign.CampaignAuditLog;
import io.spring.core.campaign.CampaignAuditLogRepository;
import io.spring.infrastructure.mybatis.mapper.CampaignAuditLogMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCampaignAuditLogRepository implements CampaignAuditLogRepository {
  private final CampaignAuditLogMapper mapper;

  public MyBatisCampaignAuditLogRepository(CampaignAuditLogMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(CampaignAuditLog log) {
    mapper.insert(log);
  }

  @Override
  public List<CampaignAuditLog> findByCampaignId(String campaignId) {
    return mapper.findByCampaignId(campaignId);
  }
}
