package io.spring.infrastructure.repository;

import io.spring.core.campaign.CampaignDecision;
import io.spring.core.campaign.CampaignDecisionRepository;
import io.spring.core.campaign.DecisionType;
import io.spring.infrastructure.mybatis.mapper.CampaignDecisionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCampaignDecisionRepository implements CampaignDecisionRepository {
  private final CampaignDecisionMapper decisionMapper;

  public MyBatisCampaignDecisionRepository(CampaignDecisionMapper decisionMapper) {
    this.decisionMapper = decisionMapper;
  }

  @Override
  public void save(CampaignDecision decision) {
    decisionMapper.insert(decision);
  }

  @Override
  public List<CampaignDecision> findByCampaignId(String campaignId) {
    return decisionMapper.findByCampaignId(campaignId);
  }

  @Override
  public int countByCampaignId(String campaignId) {
    return decisionMapper.countByCampaignId(campaignId);
  }

  @Override
  public int countByCampaignIdAndDecision(String campaignId, DecisionType decision) {
    return decisionMapper.countByCampaignIdAndDecision(campaignId, decision.name());
  }
}
