package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("decision")
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDecisionParam {
  @NotBlank private String decision;
  private String userSegment;
  private String userAgeGroup;
  private String userRegion;
}
