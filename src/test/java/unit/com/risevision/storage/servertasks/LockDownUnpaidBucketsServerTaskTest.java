package com.risevision.storage.servertasks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
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
            new GCSMockClientBuilder().build(), 
            new ActiveBucketFetcherMock(), 
            new SubscriptionStatusFetcherMock().mockFailure(), 
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

    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalTaskQueueTestConfig());
    helper.setUp();

    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    task.handleRequest();
    QueueStateInfo qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getQueue("storageBulkOperations")
                            .getQueueName());

    assertThat("the task queue is populated ", qsi.getTaskInfo().size(), is(3));
    helper.tearDown();
  }
}
