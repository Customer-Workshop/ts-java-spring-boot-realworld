package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("campaign")
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignParam {
  private String name;
  private String targetAudienceSegment;
  private String startDate;
  private String endDate;
  private String messageTitle;
  private String messageBody;
  private String messageImageUrl;
  private String messageCtaText;
  private String fulfillmentActionType;
  private String status;
  private String displayPlacement;
  private String frequencyCapType;
  private Integer frequencyCapMaxImpressions;
  private String deliveryStartTime;
  private String deliveryEndTime;
  private String personalizationTokens;
  private Integer remindLaterDeferralDays;
  private String fulfillmentWorkflowUrl;
  private Boolean declineSuppression;
  private String confirmationMessage;
  private String audienceRules;
  private String channel;
  private Integer priority;
  private String tags;
  private Boolean abTestEnabled;
}
