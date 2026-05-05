package io.spring.infrastructure.repository;

import io.spring.core.campaign.CampaignTagRepository;
import io.spring.infrastructure.mybatis.mapper.CampaignTagMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCampaignTagRepository implements CampaignTagRepository {
  private final CampaignTagMapper mapper;

  public MyBatisCampaignTagRepository(CampaignTagMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(String id, String campaignId, String tag) {
    mapper.insert(id, campaignId, tag);
  }

  @Override
  public List<String> findByCampaignId(String campaignId) {
    return mapper.findByCampaignId(campaignId);
  }

  @Override
  public void deleteByCampaignId(String campaignId) {
    mapper.deleteByCampaignId(campaignId);
  }

  @Override
  public List<String> findAllDistinctTags() {
    return mapper.findAllDistinctTags();
  }
}
