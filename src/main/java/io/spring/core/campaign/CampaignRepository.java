package io.spring.core.campaign;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {
  void save(Campaign campaign);

  Optional<Campaign> findById(String id);

  List<Campaign> findAll(boolean includeArchived);

  List<Campaign> findByStatus(CampaignStatus status, boolean includeArchived);

  void remove(Campaign campaign);
}
