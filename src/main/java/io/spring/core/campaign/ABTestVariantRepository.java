package io.spring.core.campaign;

import java.util.List;

public interface ABTestVariantRepository {
  void save(ABTestVariant variant);

  List<ABTestVariant> findByCampaignId(String campaignId);

  void deleteByCampaignId(String campaignId);

  void update(ABTestVariant variant);
}
