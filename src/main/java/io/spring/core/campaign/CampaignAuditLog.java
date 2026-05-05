package io.spring.core.campaign;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class CampaignAuditLog {
  private String id;
  private String campaignId;
  private String userId;
  private String action;
  private String fieldName;
  private String oldValue;
  private String newValue;
  private DateTime timestamp;

  public CampaignAuditLog(
      String campaignId, String userId, String action, String fieldName,
      String oldValue, String newValue) {
    this.id = UUID.randomUUID().toString();
    this.campaignId = campaignId;
    this.userId = userId;
    this.action = action;
    this.fieldName = fieldName;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.timestamp = new DateTime();
  }

  public static CampaignAuditLog created(String campaignId, String userId) {
    return new CampaignAuditLog(campaignId, userId, "CREATED", null, null, null);
  }

  public static CampaignAuditLog statusChange(
      String campaignId, String userId, String oldStatus, String newStatus) {
    return new CampaignAuditLog(campaignId, userId, "STATUS_CHANGE", "status", oldStatus, newStatus);
  }

  public static CampaignAuditLog fieldUpdate(
      String campaignId, String userId, String fieldName, String oldValue, String newValue) {
    return new CampaignAuditLog(campaignId, userId, "FIELD_UPDATE", fieldName, oldValue, newValue);
  }

  public static CampaignAuditLog cloned(String campaignId, String userId, String sourceCampaignId) {
    return new CampaignAuditLog(campaignId, userId, "CLONED", "sourceId", sourceCampaignId, null);
  }

  public static CampaignAuditLog deleted(String campaignId, String userId) {
    return new CampaignAuditLog(campaignId, userId, "DELETED", null, null, null);
  }

  public static CampaignAuditLog archived(String campaignId, String userId) {
    return new CampaignAuditLog(campaignId, userId, "ARCHIVED", null, null, null);
  }
}
