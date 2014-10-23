package com.risevision.storage.servertasks;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.lang.reflect.Method;

import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class BatchBucketObjectsUpdaterTest {

  Storage mockGCSClient = new GCSMockClientBuilder(200).build();
  Map<String, String[]> requestParams = new HashMap<>();

  @Test public void itAppliesMaxResultsAndPageToken() throws IOException {
    requestParams.put("maxResults", new String[] {"50"});
    requestParams.put("pageToken", new String[] {"abc"});

    Storage.Objects.List listRequest = 
    mockGCSClient.objects().list("bucket");

    BatchBucketObjectsUpdater batchUpdater =
    new BatchBucketObjectsUpdater(null, null, listRequest, requestParams);

    batchUpdater.setupListRequest();

    assertThat("it changed maxResults", listRequest.getMaxResults(), is(50L));
    assertThat("it changed pageToken", listRequest.getPageToken(), is("abc"));
  }

  @Test public void itGetsTheObjectList() throws IOException {
    requestParams.put("maxResults", new String[] {"50"});
    requestParams.put("pageToken", new String[] {"abc"});

    String mockListResult = "{\"items\":[{\"a\":\"1\"},{\"b\":\"2\"}]}";
    mockGCSClient = new GCSMockClientBuilder(mockListResult).build();
    Storage.Objects.List listRequest = 
    mockGCSClient.objects().list("bucket");

    BatchBucketObjectsUpdater batchUpdater =
    new BatchBucketObjectsUpdater(null, null, listRequest, requestParams);

    batchUpdater.executeListRequest();

    assertThat("it has the items", batchUpdater.listResult.getItems().size(), is(2));
  }

  @Test public void itQueuesBatchRequests() throws Exception {
    requestParams.put("maxResults", new String[] {"50"});
    requestParams.put("pageToken", new String[] {"abc"});

    String mockListResult = "{\"items\":[{\"name\":\"1\"},{\"name\":\"2\"}]}";
    mockGCSClient = new GCSMockClientBuilder(mockListResult).build();
    Storage.Objects.List listRequest = 
    mockGCSClient.objects().list("bucket");

    ObjectAccessControl acl = new ObjectAccessControl();
    acl.setEntity("allUsers").setRole("READER");

    Storage.ObjectAccessControls.Insert insertRequest = 
    mockGCSClient.objectAccessControls().insert
    ("bucket", "object", acl);

    Method iterationUpdater =
    insertRequest.getClass().getMethod("setObject", String.class);

    BatchBucketObjectsUpdater batchUpdater =
    new BatchBucketObjectsUpdater<Storage.ObjectAccessControls.Insert>
    (insertRequest, iterationUpdater, listRequest, requestParams);

    batchUpdater.setupListRequest();
    batchUpdater.executeListRequest();
    batchUpdater.queueBatchRequest();
    assertThat("queue has the items", batchUpdater.batchRequest.size(), is(2));
  }

  @Test public void itSubmitsNextTask() throws Exception {
    requestParams.put("maxResults", new String[] {"50"});
    requestParams.put("pageToken", new String[] {"abc"});

    String mockListResult =
    "{\"nextPageToken\":\"abc\",\"items\":[{\"a\":\"1\"},{\"b\":\"2\"}]}";
    mockGCSClient = new GCSMockClientBuilder(mockListResult).build();
    Storage.Objects.List listRequest = 
    mockGCSClient.objects().list("bucket");

    BatchBucketObjectsUpdater batchUpdater =
    new BatchBucketObjectsUpdater<Storage.ObjectAccessControls.Insert>
    (null, null, listRequest, requestParams);

    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalTaskQueueTestConfig());
    helper.setUp();

    LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
    QueueStateInfo qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getDefaultQueue().getQueueName());
    assertThat("the task queue is empty", qsi.getTaskInfo().size(), is(0));

    batchUpdater.setupListRequest();
    batchUpdater.executeListRequest();
    batchUpdater.submitNextTask();

    qsi = ltq.getQueueStateInfo()
                            .get(QueueFactory.getDefaultQueue().getQueueName());
    assertThat("it queued up a task", qsi.getTaskInfo().size(), is(1));
    helper.tearDown();
  }
}
