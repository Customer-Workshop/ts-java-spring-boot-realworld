package io.spring.core.campaign;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ABTestVariant {
  private String id;
  private String campaignId;
  private String variantName;
  private int splitPercentage;
  private String messageTitle;
  private String messageBody;
  private String messageCtaText;
  private String messageImageUrl;
  private int impressions;
  private int conversions;
  private boolean winner;
  private DateTime createdAt;

  public ABTestVariant(
      String campaignId,
      String variantName,
      int splitPercentage,
      String messageTitle,
      String messageBody,
      String messageCtaText,
      String messageImageUrl) {
    this.id = UUID.randomUUID().toString();
    this.campaignId = campaignId;
    this.variantName = variantName;
    this.splitPercentage = splitPercentage;
    this.messageTitle = messageTitle;
    this.messageBody = messageBody;
    this.messageCtaText = messageCtaText;
    this.messageImageUrl = messageImageUrl;
    this.impressions = 0;
    this.conversions = 0;
    this.winner = false;
    this.createdAt = new DateTime();
  }

  public void markAsWinner() {
    this.winner = true;
  }

  public void clearWinner() {
    this.winner = false;
  }

  public double getConversionRate() {
    return impressions > 0 ? (double) conversions / impressions * 100 : 0;
  }
}
