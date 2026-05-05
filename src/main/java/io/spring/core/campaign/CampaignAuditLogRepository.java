package io.spring.core.campaign;

import java.util.List;

public interface CampaignAuditLogRepository {
  void save(CampaignAuditLog log);

  List<CampaignAuditLog> findByCampaignId(String campaignId);
}
