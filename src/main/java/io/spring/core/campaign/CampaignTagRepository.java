package io.spring.core.campaign;

import java.util.List;

public interface CampaignTagRepository {
  void save(String id, String campaignId, String tag);

  List<String> findByCampaignId(String campaignId);

  void deleteByCampaignId(String campaignId);

  List<String> findAllDistinctTags();
}
