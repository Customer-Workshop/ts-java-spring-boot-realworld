package io.spring.application.campaign;

import io.spring.infrastructure.mybatis.mapper.UserEntitlementMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EntitlementService {
  public static final String MARKETING_ENTITLEMENT = "Marketing";

  private final UserEntitlementMapper entitlementMapper;

  public EntitlementService(UserEntitlementMapper entitlementMapper) {
    this.entitlementMapper = entitlementMapper;
  }

  public boolean hasMarketingEntitlement(String userId) {
    return entitlementMapper.hasEntitlement(userId, MARKETING_ENTITLEMENT);
  }

  public List<String> getUserEntitlements(String userId) {
    return entitlementMapper.findEntitlementsByUserId(userId);
  }
}
