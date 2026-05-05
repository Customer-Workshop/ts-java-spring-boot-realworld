package io.spring.infrastructure.repository;

import io.spring.core.campaign.Campaign;
import io.spring.core.campaign.CampaignRepository;
import io.spring.core.campaign.CampaignStatus;
import io.spring.infrastructure.mybatis.mapper.CampaignMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCampaignRepository implements CampaignRepository {
  private final CampaignMapper campaignMapper;

  public MyBatisCampaignRepository(CampaignMapper campaignMapper) {
    this.campaignMapper = campaignMapper;
  }

  @Override
  public void save(Campaign campaign) {
    if (campaignMapper.findById(campaign.getId()) == null) {
      campaignMapper.insert(campaign);
    } else {
      campaignMapper.update(campaign);
    }
  }

  @Override
  public Optional<Campaign> findById(String id) {
    return Optional.ofNullable(campaignMapper.findById(id));
  }

  @Override
  public List<Campaign> findAll(boolean includeArchived) {
    return campaignMapper.findAll(includeArchived);
  }

  @Override
  public List<Campaign> findByStatus(CampaignStatus status, boolean includeArchived) {
    return campaignMapper.findByStatus(status.name(), includeArchived);
  }

  @Override
  public void remove(Campaign campaign) {
    campaignMapper.delete(campaign.getId());
  }
}
