package io.spring.core.campaign;

import java.util.List;

public interface CampaignDecisionRepository {
  void save(CampaignDecision decision);

  List<CampaignDecision> findByCampaignId(String campaignId);

  int countByCampaignId(String campaignId);

  int countByCampaignIdAndDecision(String campaignId, DecisionType decision);
}
