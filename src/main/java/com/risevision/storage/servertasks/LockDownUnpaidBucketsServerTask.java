package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.storage.Storage;
import com.risevision.storage.Globals;
import com.risevision.storage.api.SubscriptionStatusFetcher;
import com.risevision.storage.api.impl.SubscriptionStatusFetcherImpl;
import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.gcs.impl.ActiveBucketFetcherImpl;

public class LockDownUnpaidBucketsServerTask extends ServerTask {
  private ActiveBucketFetcher activeBucketFetcher;
  private SubscriptionStatusFetcher statusFetcher;
  private StorageService storageService;
  private int days;
  
  public LockDownUnpaidBucketsServerTask(Storage client, Map<String, String[]> params) throws IOException {
    this(client, new ActiveBucketFetcherImpl(), new SubscriptionStatusFetcherImpl(), params);
  }

  public LockDownUnpaidBucketsServerTask(Storage client, ActiveBucketFetcher activeBucketFetcher, SubscriptionStatusFetcher statusFetcher, Map<String, String[]> params) throws IOException {
    super(client, params);
    
    confirmURLParams("days");
    
    setActiveBucketFetcher(activeBucketFetcher);
    setStatusFetcher(statusFetcher);
    setStorageService(StorageService.getInstance());
    setDays(new Integer(params.get("days")[0]));
  }

  @Override
  void handleRequest() throws IOException {
    try {
      List<String> companies = getActiveBucketFetcher().execute(getDays());
      
      for(String company : companies) {
        SubscriptionStatus status = getStatusFetcher().getSubscriptionStatus(company);
        
        if(status.isSuspended()) {
          Map<String, String[]> params = new HashMap<String, String[]>();
          
          params.put("bucket", new String[] { Globals.COMPANY_BUCKET_PREFIX + company });
          new RemovePublicReadBucketServerTask(getGcsClient(), params).handleRequest();
        }
      }
    }
    catch (Exception e) {
      throw new IOException(e);
    }
  }
  
  public ActiveBucketFetcher getActiveBucketFetcher() {
    return activeBucketFetcher;
  }
  
  public void setActiveBucketFetcher(ActiveBucketFetcher activeBucketFetcher) {
    this.activeBucketFetcher = activeBucketFetcher;
  }

  public SubscriptionStatusFetcher getStatusFetcher() {
    return statusFetcher;
  }

  public void setStatusFetcher(SubscriptionStatusFetcher statusFetcher) {
    this.statusFetcher = statusFetcher;
  }

  public StorageService getStorageService() {
    return storageService;
  }

  public void setStorageService(StorageService storageService) {
    this.storageService = storageService;
  }

  public int getDays() {
    return days;
  }

  public void setDays(int days) {
    this.days = days;
  }
}
