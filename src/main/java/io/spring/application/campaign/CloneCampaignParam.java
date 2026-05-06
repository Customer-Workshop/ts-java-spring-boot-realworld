package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("clone")
@NoArgsConstructor
@AllArgsConstructor
public class CloneCampaignParam {
  private String name;
}
