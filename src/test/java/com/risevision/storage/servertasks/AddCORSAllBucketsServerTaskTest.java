package com.risevision.storage.servertasks;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class AddCORSAllBucketsServerTaskTest {

  Map<String, String[]> requestParams = new HashMap<>();

  @Test (expected = IOException.class)
  public void itThrowsOnServerError() throws IOException {
    requestParams.put("bucket", new String[] {"bucketName"});
    requestParams.put("maxResults", new String[] {"50"});

    AddCORSAllBucketsServerTask task = new AddCORSAllBucketsServerTask
    (new GCSMockClientBuilder(404).build(), requestParams);

    task.handleRequest();
  }

  @Test public void itBatchesRequestsWithNoPageToken() throws IOException {
    requestParams.put("bucket", new String[] {"bucketName"});
    requestParams.put("maxResults", new String[] {"50"});

    String listResponse = 
    "{" +
      "\"kind\": \"storage#buckets\"," +
      "\"items\": [" +
            "{" +
              "\"kind\": \"storage#bucket\"," + 
              "\"name\": \"bucketName\"," +
              "\"cors\": []" +
            "}" +
      "]" +
    "}";

    AddCORSAllBucketsServerTask task = new AddCORSAllBucketsServerTask
    (new GCSMockClientBuilder(listResponse).build(), requestParams);

    task.setupCORSPatchRequest();
    task.setListResult();
    task.prepareBatchRequest();
    task.submitNextTask();
  }

  @Test public void itSubmitsNextTaskWithPageToken() throws IOException {
    requestParams.put("bucket", new String[] {"bucketName"});
    requestParams.put("maxResults", new String[] {"50"});

    String listResponse = 
    "{" +
      "\"kind\": \"storage#buckets\"," +
      "\"nextPageToken\": \"aaaa\"," +
      "\"items\": [" +
            "{" +
              "\"kind\": \"storage#bucket\"," + 
              "\"name\": \"bucketName\"," +
              "\"cors\": []" +
            "}" +
      "]" +
    "}";

    AddCORSAllBucketsServerTask task = new AddCORSAllBucketsServerTask
    (new GCSMockClientBuilder(listResponse).build(), requestParams);

    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalTaskQueueTestConfig());
    helper.setUp();

    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    QueueStateInfo qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getDefaultQueue().getQueueName());
    assertThat("the task queue is empty", qsi.getTaskInfo().size(), is(0));

    task.setupCORSPatchRequest();
    task.setListResult();
    task.prepareBatchRequest();
    task.submitNextTask();

    qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getDefaultQueue().getQueueName());
    assertThat("it queued up a task", qsi.getTaskInfo().size(), is(1));
    helper.tearDown();
  }
}

