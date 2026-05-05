package io.spring.core.campaign;

import java.util.List;
import java.util.Map;

public interface CampaignDecisionRepository {
  void save(CampaignDecision decision);

  List<CampaignDecision> findByCampaignId(String campaignId);

  int countByCampaignId(String campaignId);

  int countByCampaignIdAndDecision(String campaignId, DecisionType decision);

  Map<String, Integer> countAllByDecisionForNonArchived();

  void deleteByCampaignId(String campaignId);
}
