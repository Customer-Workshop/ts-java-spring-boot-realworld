package io.spring.application.campaign;

import io.spring.core.campaign.CampaignDecision;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class CampaignAnalytics {
  private final int totalTargetedPopulation;
  private final int acceptedCount;
  private final int declinedCount;
  private final int clickedUnfinishedCount;
  private final Map<String, Map<String, Integer>> commonalityBySegment;
  private final Map<String, Map<String, Integer>> commonalityByAgeGroup;
  private final Map<String, Map<String, Integer>> commonalityByRegion;

  public CampaignAnalytics(
      int totalTargetedPopulation,
      int acceptedCount,
      int declinedCount,
      int clickedUnfinishedCount,
      List<CampaignDecision> decisions) {
    this.totalTargetedPopulation = totalTargetedPopulation;
    this.acceptedCount = acceptedCount;
    this.declinedCount = declinedCount;
    this.clickedUnfinishedCount = clickedUnfinishedCount;
    this.commonalityBySegment = buildCommonality(decisions, CampaignDecision::getUserSegment);
    this.commonalityByAgeGroup = buildCommonality(decisions, CampaignDecision::getUserAgeGroup);
    this.commonalityByRegion = buildCommonality(decisions, CampaignDecision::getUserRegion);
  }

  private Map<String, Map<String, Integer>> buildCommonality(
      List<CampaignDecision> decisions,
      java.util.function.Function<CampaignDecision, String> classifier) {
    Map<String, Map<String, Integer>> result = new HashMap<>();
    for (CampaignDecision decision : decisions) {
      String key = classifier.apply(decision);
      if (key == null || key.isEmpty()) {
        key = "Unknown";
      }
      String decisionName = decision.getDecision().name();
      result.computeIfAbsent(key, k -> new HashMap<>()).merge(decisionName, 1, Integer::sum);
    }
    return result;
  }
}
