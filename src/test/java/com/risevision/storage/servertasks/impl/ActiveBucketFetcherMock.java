package com.risevision.storage.servertasks.impl;

import java.util.ArrayList;
import java.util.List;

import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.info.ServiceFailedException;

public class ActiveBucketFetcherMock implements ActiveBucketFetcher {
  @Override
  public List<String> execute(int days) throws ServiceFailedException {
    ArrayList<String> companies = new ArrayList<String>();
    
    companies.add("22e2ea3f-c4ab-4747-88ef-e6c3bddec195");
    companies.add("2543654c-c26f-444d-942f-ab390a6d92e5");
    companies.add("2cbb7001-16d8-44dd-a444-e22f811b3bd7");
    
    return companies;
  }
}
