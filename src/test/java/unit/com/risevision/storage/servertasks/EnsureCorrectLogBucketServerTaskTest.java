package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.risevision.storage.gcs.GCSMockClientBuilder;

public class EnsureCorrectLogBucketServerTaskTest {
  Map<String, String[]> requestParams = new HashMap<>();

  @Test (expected = IOException.class)
  public void itThrowsOnServerError() throws IOException {
    EnsureCorrectLogBucketServerTask task = new EnsureCorrectLogBucketServerTask
    (new GCSMockClientBuilder(404).build(), requestParams);
    
    task.handleRequest();
  }

  @Test public void itBatchesRequestsWithNoPageToken() throws IOException {
    String listResponse = getListResponse(false);
    
    requestParams.put("maxResults", new String[] {"50"});

    EnsureCorrectLogBucketServerTask task = new EnsureCorrectLogBucketServerTask
    (new GCSMockClientBuilder(listResponse).build(), requestParams);
    
    task.setListResult();
    task.prepareBatchRequest();
    task.submitNextTask();
  }

  @Test public void itSubmitsNextTaskWithPageToken() throws IOException {
    String listResponse = getListResponse(true);
    
    requestParams.put("maxResults", new String[] {"50"});

    EnsureCorrectLogBucketServerTask task = new EnsureCorrectLogBucketServerTask
    (new GCSMockClientBuilder(listResponse).build(), requestParams);

    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalTaskQueueTestConfig());
    helper.setUp();

    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    QueueStateInfo qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getQueue("storageBulkOperations").getQueueName());
    assertThat("the task queue is empty", qsi.getTaskInfo().size(), is(0));
    
    task.setListResult();
    task.prepareBatchRequest();
    task.submitNextTask();

    qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getQueue("storageBulkOperations").getQueueName());
    assertThat("it queued up a task", qsi.getTaskInfo().size(), is(1));
    helper.tearDown();
  }

  @Test public void itBatchesOnlyOneItem() throws IOException {
    String listResponse = getListResponse(false);
    
    requestParams.put("maxResults", new String[] {"50"});

    EnsureCorrectLogBucketServerTask task = new EnsureCorrectLogBucketServerTask
    (new GCSMockClientBuilder(listResponse).build(), requestParams);
    
    task.setListResult();
    task.prepareBatchRequest();
    
    assertThat("it batched a single update", task.batchRequest.size(), is(1));
  }
  
  protected String getListResponse(boolean nextPage) {
    return
    "{" +
        "\"kind\": \"storage#buckets\"," +
        (nextPage ? "\"nextPageToken\": \"aaaa\"," : "") +
        "\"items\": [" +
              "{" +
                "\"kind\": \"storage#bucket\"," + 
                "\"name\": \"risemedialibrary-subscribedBucketName\"," +
                "\"cors\": []," +
                "\"logging\":{\"logBucket\":\"bad-log-bucket\",\"logObjectPrefix\":\"risemedialibrary-subscribedBucketName\"}" +
              "}," +
              "{" +
                "\"kind\": \"storage#bucket\"," + 
                "\"name\": \"non-risemedialibrary-companyNotFoundBucketName\"," +
                "\"cors\": []," +
                "\"logging\":{\"logBucket\":\"rise-storage-logs\",\"logObjectPrefix\":\"risemedialibrary-companyNotFoundBucketName\"}" +
              "}" +
        "]" +
      "}";    
  }

}
