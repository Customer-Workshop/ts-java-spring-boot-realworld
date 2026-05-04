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
}
