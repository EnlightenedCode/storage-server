package com.risevision.storage.api;

import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.info.ServiceFailedException;

public interface SubscriptionStatusFetcher {
  public SubscriptionStatus getSubscriptionStatus(String companyId) throws ServiceFailedException;
}
