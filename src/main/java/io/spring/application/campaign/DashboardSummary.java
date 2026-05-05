package io.spring.application.campaign;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummary {
  private final int totalCampaigns;
  private final int activeCampaigns;
  private final int draftCampaigns;
  private final int pausedCampaigns;
  private final int endedCampaigns;
  private final int totalTargetedPopulation;
  private final int totalAccepted;
  private final int totalDeclined;
  private final int totalClickedUnfinished;
  private final int totalRemindLater;
  private final String lastUpdated;
}
