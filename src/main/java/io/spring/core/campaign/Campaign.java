package io.spring.core.campaign;

import io.spring.Util;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Campaign {
  private String id;
  private String name;
  private CampaignStatus status;
  private String targetAudienceSegment;
  private DateTime startDate;
  private DateTime endDate;
  private String messageTitle;
  private String messageBody;
  private String messageImageUrl;
  private String messageCtaText;
  private FulfillmentActionType fulfillmentActionType;
  private String createdBy;
  private boolean archived;
  private DateTime createdAt;
  private DateTime updatedAt;
  private String displayPlacement;
  private String frequencyCapType;
  private int frequencyCapMaxImpressions;
  private String deliveryStartTime;
  private String deliveryEndTime;
  private String personalizationTokens;
  private int remindLaterDeferralDays;
  private String fulfillmentWorkflowUrl;
  private boolean declineSuppression;
  private String confirmationMessage;
  private String audienceRules;

  public Campaign(
      String name,
      String targetAudienceSegment,
      DateTime startDate,
      DateTime endDate,
      String messageTitle,
      String messageBody,
      String messageImageUrl,
      String messageCtaText,
      FulfillmentActionType fulfillmentActionType,
      String createdBy) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
    this.status = CampaignStatus.DRAFT;
    this.targetAudienceSegment = targetAudienceSegment;
    this.startDate = startDate;
    this.endDate = endDate;
    this.messageTitle = messageTitle;
    this.messageBody = messageBody;
    this.messageImageUrl = messageImageUrl;
    this.messageCtaText = messageCtaText;
    this.fulfillmentActionType = fulfillmentActionType;
    this.createdBy = createdBy;
    this.archived = false;
    this.createdAt = new DateTime();
    this.updatedAt = this.createdAt;
    this.frequencyCapMaxImpressions = 1;
    this.remindLaterDeferralDays = 1;
    this.declineSuppression = true;
  }

  public void updateTargeting(
      String displayPlacement,
      String frequencyCapType,
      Integer frequencyCapMaxImpressions,
      String deliveryStartTime,
      String deliveryEndTime,
      String personalizationTokens,
      Integer remindLaterDeferralDays,
      String fulfillmentWorkflowUrl,
      Boolean declineSuppression,
      String confirmationMessage,
      String audienceRules) {
    if (displayPlacement != null) {
      this.displayPlacement = displayPlacement;
    }
    if (frequencyCapType != null) {
      this.frequencyCapType = frequencyCapType;
    }
    if (frequencyCapMaxImpressions != null) {
      this.frequencyCapMaxImpressions = frequencyCapMaxImpressions;
    }
    if (deliveryStartTime != null) {
      this.deliveryStartTime = deliveryStartTime;
    }
    if (deliveryEndTime != null) {
      this.deliveryEndTime = deliveryEndTime;
    }
    if (personalizationTokens != null) {
      this.personalizationTokens = personalizationTokens;
    }
    if (remindLaterDeferralDays != null) {
      this.remindLaterDeferralDays = remindLaterDeferralDays;
    }
    if (fulfillmentWorkflowUrl != null) {
      this.fulfillmentWorkflowUrl = fulfillmentWorkflowUrl;
    }
    if (declineSuppression != null) {
      this.declineSuppression = declineSuppression;
    }
    if (confirmationMessage != null) {
      this.confirmationMessage = confirmationMessage;
    }
    if (audienceRules != null) {
      this.audienceRules = audienceRules;
    }
    this.updatedAt = new DateTime();
  }

  public void update(
      String name,
      String targetAudienceSegment,
      DateTime startDate,
      DateTime endDate,
      String messageTitle,
      String messageBody,
      String messageImageUrl,
      String messageCtaText,
      FulfillmentActionType fulfillmentActionType) {
    if (!Util.isEmpty(name)) {
      this.name = name;
    }
    if (targetAudienceSegment != null) {
      this.targetAudienceSegment = targetAudienceSegment;
    }
    if (startDate != null) {
      this.startDate = startDate;
    }
    if (endDate != null) {
      this.endDate = endDate;
    }
    if (!Util.isEmpty(messageTitle)) {
      this.messageTitle = messageTitle;
    }
    if (!Util.isEmpty(messageBody)) {
      this.messageBody = messageBody;
    }
    if (messageImageUrl != null) {
      this.messageImageUrl = messageImageUrl;
    }
    if (messageCtaText != null) {
      this.messageCtaText = messageCtaText;
    }
    if (fulfillmentActionType != null) {
      this.fulfillmentActionType = fulfillmentActionType;
    }
    this.updatedAt = new DateTime();
  }

  public void updateMessageCopy(String messageTitle, String messageBody, String messageCtaText) {
    if (!Util.isEmpty(messageTitle)) {
      this.messageTitle = messageTitle;
    }
    if (!Util.isEmpty(messageBody)) {
      this.messageBody = messageBody;
    }
    if (messageCtaText != null) {
      this.messageCtaText = messageCtaText;
    }
    this.updatedAt = new DateTime();
  }

  public void activate() {
    if (this.status != CampaignStatus.DRAFT && this.status != CampaignStatus.PAUSED) {
      throw new IllegalStateException("Campaign can only be activated from DRAFT or PAUSED status");
    }
    this.status = CampaignStatus.ACTIVE;
    this.updatedAt = new DateTime();
  }

  public void pause() {
    if (this.status != CampaignStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE campaigns can be paused");
    }
    this.status = CampaignStatus.PAUSED;
    this.updatedAt = new DateTime();
  }

  public void end() {
    if (this.status != CampaignStatus.ACTIVE && this.status != CampaignStatus.PAUSED) {
      throw new IllegalStateException("Campaign can only be ended from ACTIVE or PAUSED status");
    }
    this.status = CampaignStatus.ENDED;
    this.updatedAt = new DateTime();
  }

  public void archive() {
    this.archived = true;
    this.updatedAt = new DateTime();
  }

  public void clearStartDate() {
    this.startDate = null;
    this.updatedAt = new DateTime();
  }

  public void clearEndDate() {
    this.endDate = null;
    this.updatedAt = new DateTime();
  }

  public boolean isEditable() {
    return this.status == CampaignStatus.DRAFT || this.status == CampaignStatus.PAUSED;
  }

  public boolean isDeletable() {
    return this.status == CampaignStatus.DRAFT;
  }
}
