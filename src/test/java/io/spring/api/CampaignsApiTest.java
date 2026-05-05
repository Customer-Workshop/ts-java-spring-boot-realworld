package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.campaign.CampaignAnalytics;
import io.spring.application.campaign.CampaignService;
import io.spring.application.campaign.DashboardSummary;
import io.spring.application.campaign.EntitlementService;
import io.spring.core.campaign.Campaign;
import io.spring.core.campaign.CampaignDecision;
import io.spring.core.campaign.DecisionType;
import io.spring.core.campaign.FulfillmentActionType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CampaignsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CampaignsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private CampaignService campaignService;

  @MockBean private EntitlementService entitlementService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    when(entitlementService.hasMarketingEntitlement(eq(user.getId()))).thenReturn(true);
  }

  @Test
  public void should_create_campaign_success() throws Exception {
    Map<String, Object> campaignData = new HashMap<>();
    campaignData.put("name", "Spring Sale");
    campaignData.put("targetAudienceSegment", "High-Value");
    campaignData.put("startDate", "2025-06-01T00:00:00Z");
    campaignData.put("endDate", "2025-06-30T00:00:00Z");
    campaignData.put("messageTitle", "Big Sale!");
    campaignData.put("messageBody", "Save big this spring");
    campaignData.put("fulfillmentActionType", "ACCEPT");
    Map<String, Object> param = new HashMap<>();
    param.put("campaign", campaignData);

    Campaign campaign =
        new Campaign(
            "Spring Sale",
            "High-Value",
            new DateTime(),
            new DateTime().plusDays(30),
            "Big Sale!",
            "Save big this spring",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.createCampaign(any(), eq(user.getId()))).thenReturn(campaign);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/api/campaigns")
        .then()
        .statusCode(201)
        .body("campaign.name", equalTo("Spring Sale"))
        .body("campaign.status", equalTo("DRAFT"));

    verify(campaignService).createCampaign(any(), eq(user.getId()));
  }

  @Test
  public void should_list_campaigns() throws Exception {
    Campaign campaign =
        new Campaign(
            "Test Campaign",
            "All",
            new DateTime(),
            new DateTime().plusDays(30),
            "Title",
            "Body",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.findAll(false)).thenReturn(Arrays.asList(campaign));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns")
        .then()
        .statusCode(200)
        .body("campaignsCount", equalTo(1));
  }

  @Test
  public void should_get_campaign_by_id() throws Exception {
    Campaign campaign =
        new Campaign(
            "Test Campaign",
            "All",
            new DateTime(),
            new DateTime().plusDays(30),
            "Title",
            "Body",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns/" + campaign.getId())
        .then()
        .statusCode(200)
        .body("campaign.name", equalTo("Test Campaign"));
  }

  @Test
  public void should_return_404_for_nonexistent_campaign() throws Exception {
    when(campaignService.findById(any())).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns/nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_delete_draft_campaign() throws Exception {
    Campaign campaign =
        new Campaign(
            "Draft Campaign",
            "All",
            new DateTime(),
            new DateTime().plusDays(30),
            "Title",
            "Body",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/api/campaigns/" + campaign.getId())
        .then()
        .statusCode(204);

    verify(campaignService).deleteCampaign(any());
  }

  @Test
  public void should_deny_access_without_marketing_entitlement() throws Exception {
    when(entitlementService.hasMarketingEntitlement(eq(user.getId()))).thenReturn(false);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns")
        .then()
        .statusCode(403);
  }

  @Test
  public void should_get_dashboard_summary() throws Exception {
    DashboardSummary summary = new DashboardSummary(5, 2, 1, 1, 1, 100, 50, 30, 20, 5, "2026-05-05T00:00:00.000Z");
    when(campaignService.getDashboardSummary()).thenReturn(summary);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns/dashboard")
        .then()
        .statusCode(200)
        .body("totalCampaigns", equalTo(5))
        .body("activeCampaigns", equalTo(2))
        .body("totalTargetedPopulation", equalTo(100))
        .body("totalAccepted", equalTo(50))
        .body("totalDeclined", equalTo(30));
  }

  @Test
  public void should_get_campaign_analytics() throws Exception {
    Campaign campaign =
        new Campaign(
            "Test Campaign",
            "All",
            new DateTime(),
            new DateTime().plusDays(30),
            "Title",
            "Body",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

    CampaignAnalytics analytics = new CampaignAnalytics(10, 5, 3, 2, 1, Collections.emptyList());
    when(campaignService.getAnalytics(eq(campaign.getId()))).thenReturn(analytics);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/api/campaigns/" + campaign.getId() + "/analytics")
        .then()
        .statusCode(200)
        .body("totalTargetedPopulation", equalTo(10))
        .body("acceptedCount", equalTo(5))
        .body("declinedCount", equalTo(3))
        .body("clickedUnfinishedCount", equalTo(2));
  }

  @Test
  public void should_record_campaign_decision() throws Exception {
    Campaign campaign =
        new Campaign(
            "Test Campaign",
            "All",
            new DateTime(),
            new DateTime().plusDays(30),
            "Title",
            "Body",
            null,
            null,
            FulfillmentActionType.ACCEPT,
            user.getId());

    when(campaignService.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

    CampaignDecision decision =
        new CampaignDecision(
            campaign.getId(), user.getId(), DecisionType.ACCEPTED, "High-Value", "25-30", "NA");
    when(campaignService.recordDecision(eq(campaign.getId()), eq(user.getId()), any()))
        .thenReturn(decision);

    Map<String, Object> decisionData = new HashMap<>();
    decisionData.put("decision", "ACCEPTED");
    decisionData.put("userSegment", "High-Value");
    decisionData.put("userAgeGroup", "25-30");
    decisionData.put("userRegion", "NA");
    Map<String, Object> param = new HashMap<>();
    param.put("decision", decisionData);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/api/campaigns/" + campaign.getId() + "/decisions")
        .then()
        .statusCode(201)
        .body("decision.decision", equalTo("ACCEPTED"));
  }
}
