package com.risevision.storage.servertasks.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.risevision.storage.api.SubscriptionStatusFetcher;
import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.info.ServiceFailedException;

public class SubscriptionStatusFetcherMock implements SubscriptionStatusFetcher {
  private Map<String, String> status;
  
  public SubscriptionStatusFetcherMock() {
    status = new HashMap<String, String>();
    
    status.put("22e2ea3f-c4ab-4747-88ef-e6c3bddec195", "{\"pc\":\"b0cba08a4baa0c62b8cdc621b6f6a124f89a03db\",\"status\":\"Company Not Found\",\"expiry\":null,\"trialPeriod\":0}");
    status.put("2543654c-c26f-444d-942f-ab390a6d92e5", "{\"pc\":\"b0cba08a4baa0c62b8cdc621b6f6a124f89a03db\",\"status\":\"Suspended\",\"expiry\":null,\"trialPeriod\":0}");
    status.put("2cbb7001-16d8-44dd-a444-e22f811b3bd7", "{\"pc\":\"b0cba08a4baa0c62b8cdc621b6f6a124f89a03db\",\"status\":\"Suspended\",\"expiry\":null,\"trialPeriod\":0}");
  }
  
  @Override
  public SubscriptionStatus getSubscriptionStatus(String companyId) throws ServiceFailedException {
    return new Gson().fromJson(status.get(companyId), SubscriptionStatus.class);
  }
}
