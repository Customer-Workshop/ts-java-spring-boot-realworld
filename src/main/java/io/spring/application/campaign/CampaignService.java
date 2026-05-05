package io.spring.application.campaign;

import io.spring.api.exception.InvalidCampaignStateException;
import io.spring.core.campaign.ABTestVariant;
import io.spring.core.campaign.ABTestVariantRepository;
import io.spring.core.campaign.Campaign;
import io.spring.core.campaign.CampaignAuditLog;
import io.spring.core.campaign.CampaignAuditLogRepository;
import io.spring.core.campaign.CampaignDecision;
import io.spring.core.campaign.CampaignDecisionRepository;
import io.spring.core.campaign.CampaignRepository;
import io.spring.core.campaign.CampaignStatus;
import io.spring.core.campaign.CampaignTagRepository;
import io.spring.core.campaign.DecisionType;
import io.spring.core.campaign.FulfillmentActionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class CampaignService {
  private final CampaignRepository campaignRepository;
  private final CampaignDecisionRepository decisionRepository;
  private final ABTestVariantRepository abTestVariantRepository;
  private final CampaignAuditLogRepository auditLogRepository;
  private final CampaignTagRepository tagRepository;

  public CampaignService(
      CampaignRepository campaignRepository,
      CampaignDecisionRepository decisionRepository,
      ABTestVariantRepository abTestVariantRepository,
      CampaignAuditLogRepository auditLogRepository,
      CampaignTagRepository tagRepository) {
    this.campaignRepository = campaignRepository;
    this.decisionRepository = decisionRepository;
    this.abTestVariantRepository = abTestVariantRepository;
    this.auditLogRepository = auditLogRepository;
    this.tagRepository = tagRepository;
  }

  @org.springframework.transaction.annotation.Transactional
  public Campaign createCampaign(NewCampaignParam param, String userId) {
    try {
      Campaign campaign =
          new Campaign(
              param.getName(),
              param.getTargetAudienceSegment(),
              parseDate(param.getStartDate()),
              parseDate(param.getEndDate()),
              param.getMessageTitle(),
              param.getMessageBody(),
              param.getMessageImageUrl(),
              param.getMessageCtaText(),
              FulfillmentActionType.valueOf(param.getFulfillmentActionType()),
              userId);
      campaign.updateTargeting(
          param.getDisplayPlacement(),
          param.getFrequencyCapType(),
          param.getFrequencyCapMaxImpressions(),
          param.getDeliveryStartTime(),
          param.getDeliveryEndTime(),
          param.getPersonalizationTokens(),
          param.getRemindLaterDeferralDays(),
          param.getFulfillmentWorkflowUrl(),
          param.getDeclineSuppression(),
          param.getConfirmationMessage(),
          param.getAudienceRules());
      campaign.updateIndustryFields(
          param.getChannel(), param.getPriority(), param.getTags(), param.getAbTestEnabled());
      campaignRepository.save(campaign);
      saveTags(campaign.getId(), param.getTags());
      auditLogRepository.save(CampaignAuditLog.created(campaign.getId(), userId));
      return campaign;
    } catch (IllegalArgumentException e) {
      throw new InvalidCampaignStateException("Invalid parameter: " + e.getMessage());
    }
  }

  public Optional<Campaign> findById(String id) {
    return campaignRepository.findById(id);
  }

  public List<Campaign> findAll(boolean includeArchived) {
    return campaignRepository.findAll(includeArchived);
  }

  public List<Campaign> findByStatus(CampaignStatus status, boolean includeArchived) {
    return campaignRepository.findByStatus(status, includeArchived);
  }

  @org.springframework.transaction.annotation.Transactional
  public Campaign updateCampaign(Campaign campaign, UpdateCampaignParam param, String userId) {
    try {
      String oldStatus = campaign.getStatus().name();
      boolean fieldsModified = false;
      boolean hasFieldParams =
          param.getName() != null
              || param.getTargetAudienceSegment() != null
              || param.getStartDate() != null
              || param.getEndDate() != null
              || param.getMessageTitle() != null
              || param.getMessageBody() != null
              || param.getMessageImageUrl() != null
              || param.getMessageCtaText() != null
              || param.getFulfillmentActionType() != null
              || param.getChannel() != null
              || param.getTags() != null
              || param.getDisplayPlacement() != null
              || param.getFrequencyCapType() != null
              || param.getFrequencyCapMaxImpressions() != null
              || param.getDeliveryStartTime() != null
              || param.getDeliveryEndTime() != null
              || param.getPersonalizationTokens() != null
              || param.getRemindLaterDeferralDays() != null
              || param.getFulfillmentWorkflowUrl() != null
              || param.getDeclineSuppression() != null
              || param.getConfirmationMessage() != null
              || param.getAudienceRules() != null
              || param.getPriority() != null
              || param.getAbTestEnabled() != null;
      if (campaign.isEditable() && hasFieldParams) {
        campaign.update(
            param.getName(),
            param.getTargetAudienceSegment(),
            parseDate(param.getStartDate()),
            parseDate(param.getEndDate()),
            param.getMessageTitle(),
            param.getMessageBody(),
            param.getMessageImageUrl(),
            param.getMessageCtaText(),
            param.getFulfillmentActionType() != null
                ? FulfillmentActionType.valueOf(param.getFulfillmentActionType())
                : null);
        if (param.getStartDate() != null && param.getStartDate().isEmpty()) {
          campaign.clearStartDate();
        }
        if (param.getEndDate() != null && param.getEndDate().isEmpty()) {
          campaign.clearEndDate();
        }
        campaign.updateTargeting(
            param.getDisplayPlacement(),
            param.getFrequencyCapType(),
            param.getFrequencyCapMaxImpressions(),
            param.getDeliveryStartTime(),
            param.getDeliveryEndTime(),
            param.getPersonalizationTokens(),
            param.getRemindLaterDeferralDays(),
            param.getFulfillmentWorkflowUrl(),
            param.getDeclineSuppression(),
            param.getConfirmationMessage(),
            param.getAudienceRules());
        campaign.updateIndustryFields(
            param.getChannel(), param.getPriority(), param.getTags(), param.getAbTestEnabled());
        if (param.getTags() != null) {
          saveTags(campaign.getId(), param.getTags());
        }
        fieldsModified = true;
      } else if (campaign.getStatus() == CampaignStatus.ACTIVE && hasFieldParams) {
        campaign.updateMessageCopy(
            param.getMessageTitle(), param.getMessageBody(), param.getMessageCtaText());
        fieldsModified = true;
      } else if (param.getStatus() == null && !campaign.isEditable() && campaign.getStatus() != CampaignStatus.ACTIVE) {
        throw new InvalidCampaignStateException("ENDED campaigns cannot be edited");
      }

      if (param.getStatus() != null) {
        try {
          CampaignStatus newStatus = CampaignStatus.valueOf(param.getStatus());
          switch (newStatus) {
            case ACTIVE:
              campaign.activate();
              break;
            case PAUSED:
              campaign.pause();
              break;
            case ENDED:
              campaign.end();
              break;
            default:
              throw new InvalidCampaignStateException("Cannot transition to status: " + newStatus);
          }
          auditLogRepository.save(
              CampaignAuditLog.statusChange(campaign.getId(), userId, oldStatus, newStatus.name()));
        } catch (IllegalArgumentException e) {
          throw new InvalidCampaignStateException("Invalid status: " + param.getStatus());
        } catch (IllegalStateException e) {
          throw new InvalidCampaignStateException(e.getMessage());
        }
      }

      if (fieldsModified) {
        auditLogRepository.save(
            CampaignAuditLog.fieldUpdate(campaign.getId(), userId, "campaign", null, "updated"));
      }

      campaignRepository.save(campaign);
      return campaign;
    } catch (IllegalArgumentException e) {
      throw new InvalidCampaignStateException("Invalid parameter: " + e.getMessage());
    }
  }

  @org.springframework.transaction.annotation.Transactional
  public void deleteCampaign(Campaign campaign, String userId) {
    if (campaign.isDeletable()) {
      abTestVariantRepository.deleteByCampaignId(campaign.getId());
      tagRepository.deleteByCampaignId(campaign.getId());
      decisionRepository.deleteByCampaignId(campaign.getId());
      auditLogRepository.deleteByCampaignId(campaign.getId());
      campaignRepository.remove(campaign);
    } else {
      campaign.archive();
      campaignRepository.save(campaign);
      auditLogRepository.save(CampaignAuditLog.archived(campaign.getId(), userId));
    }
  }

  @org.springframework.transaction.annotation.Transactional
  public Campaign cloneCampaign(Campaign source, String newName, String userId) {
    Campaign clone = source.cloneCampaign(newName, userId);
    campaignRepository.save(clone);
    saveTags(clone.getId(), source.getTags());
    auditLogRepository.save(CampaignAuditLog.cloned(clone.getId(), userId, source.getId()));
    return clone;
  }

  public CampaignDecision recordDecision(
      String campaignId, String userId, CampaignDecisionParam param) {
    try {
      CampaignDecision decision =
          new CampaignDecision(
              campaignId,
              userId,
              DecisionType.valueOf(param.getDecision()),
              param.getUserSegment(),
              param.getUserAgeGroup(),
              param.getUserRegion());
      decisionRepository.save(decision);
      return decision;
    } catch (IllegalArgumentException e) {
      throw new InvalidCampaignStateException("Invalid decision type: " + param.getDecision());
    }
  }

  public CampaignAnalytics getAnalytics(String campaignId) {
    int totalTargeted = decisionRepository.countByCampaignId(campaignId);
    int accepted =
        decisionRepository.countByCampaignIdAndDecision(campaignId, DecisionType.ACCEPTED);
    int declined =
        decisionRepository.countByCampaignIdAndDecision(campaignId, DecisionType.DECLINED);
    int clickedUnfinished =
        decisionRepository.countByCampaignIdAndDecision(
            campaignId, DecisionType.CLICKED_UNFINISHED);
    int remindLater =
        decisionRepository.countByCampaignIdAndDecision(campaignId, DecisionType.REMIND_LATER);

    List<CampaignDecision> decisions = decisionRepository.findByCampaignId(campaignId);

    return new CampaignAnalytics(
        totalTargeted, accepted, declined, clickedUnfinished, remindLater, decisions);
  }

  public DashboardSummary getDashboardSummary() {
    List<Campaign> allCampaigns = campaignRepository.findAll(false);
    int totalCampaigns = allCampaigns.size();
    int activeCampaigns =
        (int) allCampaigns.stream().filter(c -> c.getStatus() == CampaignStatus.ACTIVE).count();
    int draftCampaigns =
        (int) allCampaigns.stream().filter(c -> c.getStatus() == CampaignStatus.DRAFT).count();
    int pausedCampaigns =
        (int) allCampaigns.stream().filter(c -> c.getStatus() == CampaignStatus.PAUSED).count();
    int endedCampaigns =
        (int) allCampaigns.stream().filter(c -> c.getStatus() == CampaignStatus.ENDED).count();

    Map<String, Integer> decisionCounts = decisionRepository.countAllByDecisionForNonArchived();
    int totalAccepted = decisionCounts.getOrDefault(DecisionType.ACCEPTED.name(), 0);
    int totalDeclined = decisionCounts.getOrDefault(DecisionType.DECLINED.name(), 0);
    int totalClickedUnfinished =
        decisionCounts.getOrDefault(DecisionType.CLICKED_UNFINISHED.name(), 0);
    int totalRemindLater = decisionCounts.getOrDefault(DecisionType.REMIND_LATER.name(), 0);
    int totalTargeted = totalAccepted + totalDeclined + totalClickedUnfinished + totalRemindLater;

    String lastUpdated = new DateTime().toString();

    return new DashboardSummary(
        totalCampaigns,
        activeCampaigns,
        draftCampaigns,
        pausedCampaigns,
        endedCampaigns,
        totalTargeted,
        totalAccepted,
        totalDeclined,
        totalClickedUnfinished,
        totalRemindLater,
        lastUpdated);
  }

  // A/B Test methods
  public List<ABTestVariant> getABTestVariants(String campaignId) {
    return abTestVariantRepository.findByCampaignId(campaignId);
  }

  public ABTestVariant createABTestVariant(String campaignId, ABTestVariantParam param) {
    ABTestVariant variant =
        new ABTestVariant(
            campaignId,
            param.getVariantName(),
            param.getSplitPercentage(),
            param.getMessageTitle(),
            param.getMessageBody(),
            param.getMessageCtaText(),
            param.getMessageImageUrl());
    abTestVariantRepository.save(variant);
    return variant;
  }

  public void deleteABTestVariants(String campaignId) {
    abTestVariantRepository.deleteByCampaignId(campaignId);
  }

  public ABTestVariant declareWinner(String campaignId, String variantId) {
    List<ABTestVariant> variants = abTestVariantRepository.findByCampaignId(campaignId);
    ABTestVariant winner = null;
    for (ABTestVariant v : variants) {
      if (v.getId().equals(variantId)) {
        v.markAsWinner();
        winner = v;
      } else {
        v.clearWinner();
      }
      abTestVariantRepository.update(v);
    }
    if (winner == null) {
      throw new InvalidCampaignStateException("Variant not found: " + variantId);
    }
    return winner;
  }

  // Tag methods
  public List<String> getTags(String campaignId) {
    return tagRepository.findByCampaignId(campaignId);
  }

  public List<String> getAllDistinctTags() {
    return tagRepository.findAllDistinctTags();
  }

  // Audit Log methods
  public List<CampaignAuditLog> getAuditLog(String campaignId) {
    return auditLogRepository.findByCampaignId(campaignId);
  }

  // Bulk status update
  @org.springframework.transaction.annotation.Transactional
  public int bulkUpdateStatus(List<String> campaignIds, String newStatus, String userId) {
    CampaignStatus targetStatus;
    try {
      targetStatus = CampaignStatus.valueOf(newStatus);
    } catch (IllegalArgumentException e) {
      throw new InvalidCampaignStateException("Invalid status: " + newStatus);
    }
    int updated = 0;
    for (String id : campaignIds) {
      Optional<Campaign> opt = campaignRepository.findById(id);
      if (opt.isPresent()) {
        Campaign campaign = opt.get();
        String oldStatus = campaign.getStatus().name();
        try {
          switch (targetStatus) {
            case ACTIVE:
              campaign.activate();
              break;
            case PAUSED:
              campaign.pause();
              break;
            case ENDED:
              campaign.end();
              break;
            default:
              continue;
          }
          campaignRepository.save(campaign);
          auditLogRepository.save(
              CampaignAuditLog.statusChange(id, userId, oldStatus, targetStatus.name()));
          updated++;
        } catch (IllegalStateException e) {
          // skip campaigns that can't transition
        }
      }
    }
    return updated;
  }

  private void saveTags(String campaignId, String tagsStr) {
    if (tagsStr == null) {
      return;
    }
    tagRepository.deleteByCampaignId(campaignId);
    String[] tags = tagsStr.split(",");
    for (String tag : tags) {
      String trimmed = tag.trim();
      if (!trimmed.isEmpty()) {
        tagRepository.save(UUID.randomUUID().toString(), campaignId, trimmed);
      }
    }
  }

  private DateTime parseDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return null;
    }
    return ISODateTimeFormat.dateTimeParser().withZoneUTC().parseDateTime(dateStr);
  }
}
