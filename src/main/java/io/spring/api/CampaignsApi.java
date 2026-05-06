package io.spring.api;

import io.spring.api.exception.InvalidCampaignStateException;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.campaign.ABTestVariantParam;
import io.spring.application.campaign.CampaignAnalytics;
import io.spring.application.campaign.CampaignDecisionParam;
import io.spring.application.campaign.CampaignService;
import io.spring.application.campaign.DashboardSummary;
import io.spring.application.campaign.EntitlementService;
import io.spring.application.campaign.NewCampaignParam;
import io.spring.application.campaign.UpdateCampaignParam;
import io.spring.core.campaign.ABTestVariant;
import io.spring.core.campaign.Campaign;
import io.spring.core.campaign.CampaignAuditLog;
import io.spring.core.campaign.CampaignDecision;
import io.spring.core.campaign.CampaignStatus;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/campaigns")
@AllArgsConstructor
public class CampaignsApi {
  private final CampaignService campaignService;
  private final EntitlementService entitlementService;

  @PostMapping
  public ResponseEntity<?> createCampaign(
      @Valid @RequestBody NewCampaignParam param, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    Campaign campaign = campaignService.createCampaign(param, user.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(campaignResponse(campaign));
  }

  @GetMapping
  public ResponseEntity<?> listCampaigns(
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "includeArchived", defaultValue = "false") boolean includeArchived,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    List<Campaign> campaigns;
    if (status != null && !status.isEmpty()) {
      try {
        campaigns =
            campaignService.findByStatus(
                CampaignStatus.valueOf(status.toUpperCase()), includeArchived);
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Invalid status: " + status));
      }
    } else {
      campaigns = campaignService.findAll(includeArchived);
    }
    List<Map<String, Object>> campaignList =
        campaigns.stream().map(this::campaignToMap).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("campaigns", campaignList);
    response.put("campaignsCount", campaignList.size());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getCampaign(
      @PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    Campaign campaign = campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(campaignResponse(campaign));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateCampaign(
      @PathVariable String id,
      @Valid @RequestBody UpdateCampaignParam param,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    Campaign campaign = campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    Campaign updated = campaignService.updateCampaign(campaign, param, user.getId());
    return ResponseEntity.ok(campaignResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteCampaign(
      @PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    Campaign campaign = campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    campaignService.deleteCampaign(campaign, user.getId());
    return ResponseEntity.noContent().build();
  }

  // Clone campaign
  @PostMapping("/{id}/clone")
  public ResponseEntity<?> cloneCampaign(
      @PathVariable String id,
      @RequestBody(required = false) io.spring.application.campaign.CloneCampaignParam param,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    Campaign source = campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    String newName = param != null ? param.getName() : null;
    Campaign clone = campaignService.cloneCampaign(source, newName, user.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(campaignResponse(clone));
  }

  // Bulk status update
  @PostMapping("/bulk/status")
  public ResponseEntity<?> bulkUpdateStatus(
      @Valid @RequestBody io.spring.application.campaign.BulkStatusUpdateParam param,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    if (param.getCampaignIds() == null || param.getStatus() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "campaignIds and status required"));
    }
    int updated =
        campaignService.bulkUpdateStatus(param.getCampaignIds(), param.getStatus(), user.getId());
    return ResponseEntity.ok(Map.of("updated", updated));
  }

  // A/B Test Variants
  @GetMapping("/{id}/variants")
  public ResponseEntity<?> getVariants(
      @PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    List<ABTestVariant> variants = campaignService.getABTestVariants(id);
    List<Map<String, Object>> variantList =
        variants.stream().map(this::variantToMap).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("variants", variantList));
  }

  @PostMapping("/{id}/variants")
  public ResponseEntity<?> createVariant(
      @PathVariable String id,
      @Valid @RequestBody ABTestVariantParam param,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    ABTestVariant variant = campaignService.createABTestVariant(id, param);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("variant", variantToMap(variant)));
  }

  @PostMapping("/{id}/variants/{variantId}/winner")
  public ResponseEntity<?> declareWinner(
      @PathVariable String id,
      @PathVariable String variantId,
      @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    ABTestVariant winner = campaignService.declareWinner(id, variantId);
    return ResponseEntity.ok(Map.of("variant", variantToMap(winner)));
  }

  // Tags
  @GetMapping("/{id}/tags")
  public ResponseEntity<?> getTags(@PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    List<String> tags = campaignService.getTags(id);
    return ResponseEntity.ok(Map.of("tags", tags));
  }

  @GetMapping("/tags/all")
  public ResponseEntity<?> getAllTags(@AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    return ResponseEntity.ok(Map.of("tags", campaignService.getAllDistinctTags()));
  }

  // Audit Trail
  @GetMapping("/{id}/audit")
  public ResponseEntity<?> getAuditLog(
      @PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    List<CampaignAuditLog> logs = campaignService.getAuditLog(id);
    List<Map<String, Object>> logList =
        logs.stream().map(this::auditLogToMap).collect(Collectors.toList());
    return ResponseEntity.ok(Map.of("auditLog", logList));
  }

  @PostMapping("/{id}/decisions")
  public ResponseEntity<?> recordDecision(
      @PathVariable String id,
      @Valid @RequestBody CampaignDecisionParam param,
      @AuthenticationPrincipal User user) {
    Campaign campaign = campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (campaign.getStatus() != CampaignStatus.ACTIVE) {
      throw new InvalidCampaignStateException("Decisions can only be recorded for ACTIVE campaigns");
    }
    CampaignDecision decision = campaignService.recordDecision(id, user.getId(), param);
    Map<String, Object> response = new HashMap<>();
    response.put("decision", decisionToMap(decision));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}/analytics")
  public ResponseEntity<?> getCampaignAnalytics(
      @PathVariable String id, @AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    campaignService.findById(id).orElseThrow(ResourceNotFoundException::new);
    CampaignAnalytics analytics = campaignService.getAnalytics(id);
    return ResponseEntity.ok(analyticsToMap(analytics));
  }

  @GetMapping("/dashboard")
  public ResponseEntity<?> getDashboard(@AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    DashboardSummary summary = campaignService.getDashboardSummary();
    Map<String, Object> response = new HashMap<>();
    response.put("totalCampaigns", summary.getTotalCampaigns());
    response.put("activeCampaigns", summary.getActiveCampaigns());
    response.put("draftCampaigns", summary.getDraftCampaigns());
    response.put("pausedCampaigns", summary.getPausedCampaigns());
    response.put("endedCampaigns", summary.getEndedCampaigns());
    response.put("totalTargetedPopulation", summary.getTotalTargetedPopulation());
    response.put("totalAccepted", summary.getTotalAccepted());
    response.put("totalDeclined", summary.getTotalDeclined());
    response.put("totalClickedUnfinished", summary.getTotalClickedUnfinished());
    response.put("totalRemindLater", summary.getTotalRemindLater());
    response.put("lastUpdated", summary.getLastUpdated());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/dashboard/export")
  public ResponseEntity<byte[]> exportDashboard(@AuthenticationPrincipal User user) {
    checkMarketingEntitlement(user);
    List<Campaign> campaigns = campaignService.findAll(false);
    StringBuilder csv = new StringBuilder();
    csv.append(
        "Campaign Name,Status,Target Segment,Start Date,End Date,Fulfillment Type,"
            + "Display Placement,Frequency Cap,Delivery Window,Channel,Priority,Tags\n");
    for (Campaign c : campaigns) {
      csv.append(escapeCsv(c.getName())).append(',');
      csv.append(c.getStatus().name()).append(',');
      csv.append(escapeCsv(c.getTargetAudienceSegment())).append(',');
      csv.append(c.getStartDate() != null ? c.getStartDate().toDateTime(org.joda.time.DateTimeZone.UTC).toString("yyyy-MM-dd") : "")
          .append(',');
      csv.append(c.getEndDate() != null ? c.getEndDate().toDateTime(org.joda.time.DateTimeZone.UTC).toString("yyyy-MM-dd") : "").append(',');
      csv.append(c.getFulfillmentActionType().name()).append(',');
      csv.append(escapeCsv(c.getDisplayPlacement())).append(',');
      csv.append(escapeCsv(c.getFrequencyCapType())).append(',');
      String window =
          (c.getDeliveryStartTime() != null ? c.getDeliveryStartTime() : "")
              + "-"
              + (c.getDeliveryEndTime() != null ? c.getDeliveryEndTime() : "");
      csv.append(escapeCsv(window)).append(',');
      csv.append(escapeCsv(c.getChannel())).append(',');
      csv.append(c.getPriority()).append(',');
      csv.append(escapeCsv(c.getTags())).append('\n');
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campaigns_export.csv");
    return new ResponseEntity<>(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), headers, HttpStatus.OK);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.length() > 0 && "=+-@\t\r\n".indexOf(value.charAt(0)) >= 0) {
      value = "'" + value;
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  private void checkMarketingEntitlement(User user) {
    if (!entitlementService.hasMarketingEntitlement(user.getId())) {
      throw new NoAuthorizationException();
    }
  }

  private Map<String, Object> campaignResponse(Campaign campaign) {
    Map<String, Object> response = new HashMap<>();
    response.put("campaign", campaignToMap(campaign));
    return response;
  }

  private Map<String, Object> campaignToMap(Campaign campaign) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", campaign.getId());
    map.put("name", campaign.getName());
    map.put("status", campaign.getStatus().name());
    map.put("targetAudienceSegment", campaign.getTargetAudienceSegment());
    map.put("startDate", campaign.getStartDate());
    map.put("endDate", campaign.getEndDate());
    map.put("messageTitle", campaign.getMessageTitle());
    map.put("messageBody", campaign.getMessageBody());
    map.put("messageImageUrl", campaign.getMessageImageUrl());
    map.put("messageCtaText", campaign.getMessageCtaText());
    map.put("fulfillmentActionType", campaign.getFulfillmentActionType().name());
    map.put("createdBy", campaign.getCreatedBy());
    map.put("archived", campaign.isArchived());
    map.put("createdAt", campaign.getCreatedAt());
    map.put("updatedAt", campaign.getUpdatedAt());
    map.put("displayPlacement", campaign.getDisplayPlacement());
    map.put("frequencyCapType", campaign.getFrequencyCapType());
    map.put("frequencyCapMaxImpressions", campaign.getFrequencyCapMaxImpressions());
    map.put("deliveryStartTime", campaign.getDeliveryStartTime());
    map.put("deliveryEndTime", campaign.getDeliveryEndTime());
    map.put("personalizationTokens", campaign.getPersonalizationTokens());
    map.put("remindLaterDeferralDays", campaign.getRemindLaterDeferralDays());
    map.put("fulfillmentWorkflowUrl", campaign.getFulfillmentWorkflowUrl());
    map.put("declineSuppression", campaign.isDeclineSuppression());
    map.put("confirmationMessage", campaign.getConfirmationMessage());
    map.put("audienceRules", campaign.getAudienceRules());
    map.put("channel", campaign.getChannel());
    map.put("priority", campaign.getPriority());
    map.put("tags", campaign.getTags());
    map.put("abTestEnabled", campaign.isAbTestEnabled());
    return map;
  }

  private Map<String, Object> variantToMap(ABTestVariant variant) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", variant.getId());
    map.put("campaignId", variant.getCampaignId());
    map.put("variantName", variant.getVariantName());
    map.put("splitPercentage", variant.getSplitPercentage());
    map.put("messageTitle", variant.getMessageTitle());
    map.put("messageBody", variant.getMessageBody());
    map.put("messageCtaText", variant.getMessageCtaText());
    map.put("messageImageUrl", variant.getMessageImageUrl());
    map.put("impressions", variant.getImpressions());
    map.put("conversions", variant.getConversions());
    map.put("conversionRate", variant.getConversionRate());
    map.put("winner", variant.isWinner());
    map.put("createdAt", variant.getCreatedAt());
    return map;
  }

  private Map<String, Object> auditLogToMap(CampaignAuditLog log) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", log.getId());
    map.put("campaignId", log.getCampaignId());
    map.put("userId", log.getUserId());
    map.put("action", log.getAction());
    map.put("fieldName", log.getFieldName());
    map.put("oldValue", log.getOldValue());
    map.put("newValue", log.getNewValue());
    map.put("timestamp", log.getTimestamp());
    return map;
  }

  private Map<String, Object> decisionToMap(CampaignDecision decision) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", decision.getId());
    map.put("campaignId", decision.getCampaignId());
    map.put("userId", decision.getUserId());
    map.put("decision", decision.getDecision().name());
    map.put("userSegment", decision.getUserSegment());
    map.put("userAgeGroup", decision.getUserAgeGroup());
    map.put("userRegion", decision.getUserRegion());
    map.put("decidedAt", decision.getDecidedAt());
    return map;
  }

  private Map<String, Object> analyticsToMap(CampaignAnalytics analytics) {
    Map<String, Object> map = new HashMap<>();
    map.put("totalTargetedPopulation", analytics.getTotalTargetedPopulation());
    map.put("acceptedCount", analytics.getAcceptedCount());
    map.put("declinedCount", analytics.getDeclinedCount());
    map.put("clickedUnfinishedCount", analytics.getClickedUnfinishedCount());
    map.put("remindLaterCount", analytics.getRemindLaterCount());
    map.put("commonalityBySegment", analytics.getCommonalityBySegment());
    map.put("commonalityByAgeGroup", analytics.getCommonalityByAgeGroup());
    map.put("commonalityByRegion", analytics.getCommonalityByRegion());
    return map;
  }
}
