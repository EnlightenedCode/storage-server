package com.risevision.storage.gcs;

import java.util.List;

import com.risevision.storage.info.ServiceFailedException;

public interface ActiveBucketFetcher {
  public List<String> execute(int days) throws ServiceFailedException;
}
