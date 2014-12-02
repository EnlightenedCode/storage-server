package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.risevision.storage.servertasks.impl.ActiveBucketFetcherMock;
import com.risevision.storage.servertasks.impl.SubscriptionStatusFetcherMock;

public class LockDownUnpaidBucketsServerTaskTest {
  Map<String, String[]> requestParams = new HashMap<>();
  
  public LockDownUnpaidBucketsServerTaskTest() {
    requestParams.put("days", new String[] { "1" });
  }

  @Test (expected = IOException.class)
  public void itThrowsOnServerError() throws IOException {
    LockDownUnpaidBucketsServerTask task = 
        new LockDownUnpaidBucketsServerTask(
            new GCSMockClientBuilder(404).build(), 
            new ActiveBucketFetcherMock(), 
            new SubscriptionStatusFetcherMock(), 
            requestParams);

    task.handleRequest();
  }
  
  @Test public void itReturnsOnSuccess() throws IOException {
    LockDownUnpaidBucketsServerTask task = 
        new LockDownUnpaidBucketsServerTask(
            new GCSMockClientBuilder().build(), 
            new ActiveBucketFetcherMock(), 
            new SubscriptionStatusFetcherMock(), 
            requestParams);

    task.handleRequest();
  }
}
