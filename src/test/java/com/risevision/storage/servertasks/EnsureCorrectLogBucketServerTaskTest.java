package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.risevision.storage.gcs.GCSMockClientBuilder;

public class EnsureCorrectLogBucketServerTaskTest {
  protected static final Logger log = Logger.getAnonymousLogger();

  Map<String, String[]> requestParams = new HashMap<>();

  @Test (expected = IOException.class)
  public void itThrowsOnServerError() throws IOException {
    log.info("Verifying throws on error");
    EnsureCorrectLogBucketServerTask task = new EnsureCorrectLogBucketServerTask
    (new GCSMockClientBuilder(404).build(), requestParams);
    
    task.handleRequest();
  }

  @Test public void itBatchesRequestsWithNoPageToken() throws IOException {
    log.info("handles page token");
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
    
    log.info("submits into task queue");
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
    log.info("batches only one item");
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
                "\"logging\":{\"logBucket\":\"rise-storage-logs\",\"logObjectPrefix\":\"risemedialibrary-subscribedBucketName\"}" +
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
