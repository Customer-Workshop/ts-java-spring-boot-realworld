package io.spring.application.campaign;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("variant")
@NoArgsConstructor
@AllArgsConstructor
public class ABTestVariantParam {
  @NotBlank private String variantName;
  private int splitPercentage;
  private String messageTitle;
  private String messageBody;
  private String messageCtaText;
  private String messageImageUrl;
}
