package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;
import com.google.api.client.util.GenericData;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.QueueFactory;

public class SetObjectAclsAllBucketsServerTask extends BatchServerTask {

  public SetObjectAclsAllBucketsServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
  }

  public void handleRequest() throws IOException {
    List<TaskOptions> taskList = new ArrayList<>();
    TaskOptions taskItem;

    log.info("Enqueuing an object acl update task for every bucket");

    listRequest = new ListRequestGenerator(gcsClient).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    listResult = (GenericData) listRequest.execute();

    for (GenericData bucketItem : (List<GenericData>) listResult.get("items")) {
      taskItem = TaskOptions.Builder.withUrl("/servertask")
                 .param("task", "SetObjectAclsToDefault")
                 .param("bucket", (String) bucketItem.get("name"))
                 .method(TaskOptions.Method.valueOf("GET"));

      taskList.add(taskItem);
    }

    QueueFactory.getQueue("storageBulkOperations").add(taskList);
    submitNextTask();
  }
}


