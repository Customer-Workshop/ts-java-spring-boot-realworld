package io.spring.application.campaign;

import io.spring.api.exception.InvalidCampaignStateException;
import io.spring.core.campaign.Campaign;
import io.spring.core.campaign.CampaignDecision;
import io.spring.core.campaign.CampaignDecisionRepository;
import io.spring.core.campaign.CampaignRepository;
import io.spring.core.campaign.CampaignStatus;
import io.spring.core.campaign.DecisionType;
import io.spring.core.campaign.FulfillmentActionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class CampaignService {
  private final CampaignRepository campaignRepository;
  private final CampaignDecisionRepository decisionRepository;

  public CampaignService(
      CampaignRepository campaignRepository, CampaignDecisionRepository decisionRepository) {
    this.campaignRepository = campaignRepository;
    this.decisionRepository = decisionRepository;
  }

  public Campaign createCampaign(NewCampaignParam param, String userId) {
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
    campaignRepository.save(campaign);
    return campaign;
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

  public Campaign updateCampaign(Campaign campaign, UpdateCampaignParam param) {
    if (campaign.isEditable()) {
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
    } else if (campaign.getStatus() == CampaignStatus.ACTIVE) {
      campaign.updateMessageCopy(
          param.getMessageTitle(), param.getMessageBody(), param.getMessageCtaText());
    } else {
      throw new InvalidCampaignStateException("ENDED campaigns cannot be edited");
    }

    if (param.getStatus() != null) {
      CampaignStatus newStatus = CampaignStatus.valueOf(param.getStatus());
      try {
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
            break;
        }
      } catch (IllegalStateException e) {
        throw new InvalidCampaignStateException(e.getMessage());
      }
    }

    campaignRepository.save(campaign);
    return campaign;
  }

  public void deleteCampaign(Campaign campaign) {
    if (campaign.isDeletable()) {
      decisionRepository.deleteByCampaignId(campaign.getId());
      campaignRepository.remove(campaign);
    } else {
      campaign.archive();
      campaignRepository.save(campaign);
    }
  }

  public CampaignDecision recordDecision(
      String campaignId, String userId, CampaignDecisionParam param) {
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

    List<CampaignDecision> decisions = decisionRepository.findByCampaignId(campaignId);

    return new CampaignAnalytics(totalTargeted, accepted, declined, clickedUnfinished, decisions);
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
    int totalTargeted = totalAccepted + totalDeclined + totalClickedUnfinished;

    return new DashboardSummary(
        totalCampaigns,
        activeCampaigns,
        draftCampaigns,
        pausedCampaigns,
        endedCampaigns,
        totalTargeted,
        totalAccepted,
        totalDeclined,
        totalClickedUnfinished);
  }

  private DateTime parseDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return null;
    }
    return ISODateTimeFormat.dateTimeParser().parseDateTime(dateStr);
  }
}
