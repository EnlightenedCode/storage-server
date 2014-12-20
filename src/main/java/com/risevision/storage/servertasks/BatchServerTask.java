package com.risevision.storage.servertasks;

import java.util.Map;
import java.util.List;
import java.io.IOException;

import java.util.logging.Logger;
import com.google.common.base.Strings;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;
import com.google.api.client.util.GenericData;
import com.google.api.client.googleapis.batch.*;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.QueueFactory;

import com.risevision.storage.Globals;

abstract class BatchServerTask extends ServerTask {
  StorageRequest listRequest;
  StorageRequest iteratingRequest;
  GenericData listResult;
  BatchRequest batchRequest;
  String pageToken;
  Long maxResults;
  private final com.google.appengine.api.taskqueue.TaskOptions.Method queueMethod = 
  com.google.appengine.api.taskqueue.TaskOptions.Method.GET;

  BatchServerTask(Storage client, Map<String, String[]> params) throws IOException {
    super(client, params);
    pageToken = params.containsKey("pageToken") ?
                params.get("pageToken")[0] : null;
    maxResults = params.containsKey("maxResults") ?
                 Long.valueOf(params.get("maxResults")[0]) : 50L;
  }

  void prepareBatchRequest() throws IOException {
    BatchRequestGenerator gen = new BatchRequestGenerator
    (gcsClient
    , new StorageRequestUpdater(iteratingRequest)
    , (List<GenericData>)listResult.get("items"));

    batchRequest = gen.generateBatch();
  }

  void submitBatchRequest() throws IOException {
    if(batchRequest.size() > 0) {
      batchRequest.execute();
      log.info("Batch request complete");
    }
  }

  void submitNextTask() throws IOException {
    if ((String) listResult.get("nextPageToken") == null) {return;}

    TaskOptions options = TaskOptions.Builder.withUrl("/servertask");
    for (String param : requestParams.keySet()) {
      if (!param.equals("pageToken")) {
        options.param(param, requestParams.get(param)[0]);
      }
    }

    options.param("pageToken", (String)listResult.get("nextPageToken"));
    if (requestParams.get("task") == null) {
      String className = this.getClass().getSimpleName();
      String taskName = className.substring(0, className.lastIndexOf("ServerTask"));
      options.param("task", taskName);
    }

    options.method(queueMethod);
    QueueFactory.getDefaultQueue().add(options);
  }
}

