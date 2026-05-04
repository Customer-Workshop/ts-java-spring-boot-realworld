package io.spring.core.campaign;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class CampaignDecision {
  private String id;
  private String campaignId;
  private String userId;
  private DecisionType decision;
  private String userSegment;
  private String userAgeGroup;
  private String userRegion;
  private DateTime decidedAt;

  public CampaignDecision(
      String campaignId,
      String userId,
      DecisionType decision,
      String userSegment,
      String userAgeGroup,
      String userRegion) {
    this.id = UUID.randomUUID().toString();
    this.campaignId = campaignId;
    this.userId = userId;
    this.decision = decision;
    this.userSegment = userSegment;
    this.userAgeGroup = userAgeGroup;
    this.userRegion = userRegion;
    this.decidedAt = new DateTime();
  }
}
