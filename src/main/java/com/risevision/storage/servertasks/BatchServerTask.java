package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.util.GenericData;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageRequest;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.risevision.storage.servertasks.requestupdater.SimpleStorageRequestUpdater;
import com.risevision.storage.servertasks.requestupdater.StorageRequestUpdater;

abstract class BatchServerTask extends ServerTask {
  StorageRequest listRequest;
  StorageRequest iteratingRequest;
  GenericData listResult;
  BatchRequest batchRequest;
  String pageToken;
  Long maxResults;

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
    , createStorageRequestUpdater(iteratingRequest)
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
    if ((String) listResult.get("nextPageToken") == null) {
      log.info("Finished processing batches for " + getClass().getSimpleName());
      return;
    }

    TaskOptions options = TaskOptions.Builder.withUrl("/servertask");
    for (String param : requestParams.keySet()) {
      if (!param.equals("pageToken")) {
        options.param(param, requestParams.get(param)[0]);
      }
    }

    GenericData bqJobReference = (GenericData) listResult.get("jobReference");
    if (requestParams.get("jobId") == null && bqJobReference != null) {
      options.param("jobId", (String)bqJobReference.get("jobId"));
    }

    options.param("pageToken", (String)listResult.get("nextPageToken"));

    if (requestParams.get("task") == null) {
      String className = this.getClass().getSimpleName();
      String taskName = className.substring(0, className.lastIndexOf("ServerTask"));
      options.param("task", taskName);
    }

    options.method(TaskOptions.Method.valueOf("GET"));
    QueueFactory.getQueue("storageBulkOperations").add(options);
  }
  
  protected StorageRequestUpdater createStorageRequestUpdater(StorageRequest<?> iteratingRequest) {
    return new SimpleStorageRequestUpdater(iteratingRequest);
  }
}
