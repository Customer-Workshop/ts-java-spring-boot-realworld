package io.spring.infrastructure.repository;

import io.spring.core.campaign.ABTestVariant;
import io.spring.core.campaign.ABTestVariantRepository;
import io.spring.infrastructure.mybatis.mapper.ABTestVariantMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisABTestVariantRepository implements ABTestVariantRepository {
  private final ABTestVariantMapper mapper;

  public MyBatisABTestVariantRepository(ABTestVariantMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(ABTestVariant variant) {
    mapper.insert(variant);
  }

  @Override
  public List<ABTestVariant> findByCampaignId(String campaignId) {
    return mapper.findByCampaignId(campaignId);
  }

  @Override
  public void deleteByCampaignId(String campaignId) {
    mapper.deleteByCampaignId(campaignId);
  }

  @Override
  public void update(ABTestVariant variant) {
    mapper.update(variant);
  }
}
