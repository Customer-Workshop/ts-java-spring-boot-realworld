package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("campaign")
@NoArgsConstructor
@AllArgsConstructor
public class NewCampaignParam {
  @NotBlank private String name;
  private String targetAudienceSegment;
  private String startDate;
  private String endDate;
  private String messageTitle;
  private String messageBody;
  private String messageImageUrl;
  private String messageCtaText;
  @NotNull private String fulfillmentActionType;
}
