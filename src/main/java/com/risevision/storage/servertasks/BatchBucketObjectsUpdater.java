package com.risevision.storage.servertasks;

import java.util.Map;
import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.reflect.Method;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.*;
import com.google.api.client.googleapis.batch.*;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.json.GenericJson;
import com.google.api.client.http.HttpHeaders;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import java.util.logging.Logger;

class BatchBucketObjectsUpdater <T extends StorageRequest> {
  private static final Logger log = Logger.getAnonymousLogger();
  StorageRequest iterativeObjectRequest;
  Storage.Objects.List listRequest;
  Map<String, String[]> requestParams;
  Objects listResult;
  BatchRequest batchRequest;
  Method iterativeObjectNameUpdateMethod;
  ArrayList<GoogleJsonError> failures = new ArrayList<GoogleJsonError>();
  private final com.google.appengine.api.taskqueue.TaskOptions.Method queueMethod = 
  com.google.appengine.api.taskqueue.TaskOptions.Method.GET;

  BatchBucketObjectsUpdater
  (StorageRequest iterativeObjectRequest, Method iterativeObjectNameUpdateMethod,
  Storage.Objects.List listRequest, Map<String, String[]> requestParams) {
    this.listRequest = listRequest;
    this.requestParams = requestParams;
    this.iterativeObjectRequest = iterativeObjectRequest;
    this.iterativeObjectNameUpdateMethod = iterativeObjectNameUpdateMethod;
  }

  public void beginUpdate() throws IOException {
    this.setupListRequest();
    this.executeListRequest();
    this.queueBatchRequest();
    this.submitBatchRequest();
    this.submitNextTask();
  }

  void setupListRequest() {
    if (listRequest.getMaxResults() == null || listRequest.getMaxResults() == 0) {
      listRequest.setMaxResults(20L);
    }

    if (requestParams.get("pageToken") != null) {
      listRequest.setPageToken(requestParams.get("pageToken")[0]);
    }
    if (requestParams.get("maxResults") != null) {
      listRequest.setMaxResults
      (Long.valueOf(requestParams.get("maxResults")[0]));
    }
  }

  void executeListRequest() throws IOException {
    listResult = listRequest.execute();
  }

  void queueBatchRequest() throws IOException {
    batchRequest = iterativeObjectRequest.getAbstractGoogleClient().batch();

    JsonBatchCallback batchCallback = new JsonBatchCallback<GenericJson>(){
      public void onSuccess(GenericJson resp, HttpHeaders responseHeaders) {
        log.info("Batch task success");
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        log.warning(e.getMessage());
        failures.add(e);
      }
    };

    for (StorageObject storageObject : listResult.getItems()) {
      T iterationRequest = (T) iterativeObjectRequest.clone();
      try {
        iterativeObjectNameUpdateMethod.invoke
        (iterationRequest, storageObject.getName());
      } catch (Exception e) {
        throw new IOException("Method invocation error");
      }

      iterationRequest.queue
      (batchRequest, GoogleJsonErrorContainer.class, batchCallback);

      log.info("Added " + storageObject.getName() + " to batch");
    }
  }

  void submitBatchRequest() throws IOException {
    log.info("Batch request size: " + String.valueOf(batchRequest.size()));
    batchRequest.execute();
    log.info("Batch request complete");
    if (failures.size() > 0) {
      log.warning("There were errors on some requests");
    }
  }

  void submitNextTask() throws IOException {
    if (listResult.getNextPageToken() == null) {return;}

    TaskOptions options = TaskOptions.Builder.withUrl("/servertask");
    for (String param : requestParams.keySet()) {
      options.param(param, requestParams.get(param)[0]);
    }

    options.removeParam("pageToken");
    options.param("pageToken", listResult.getNextPageToken());
    options.method(queueMethod);

    QueueFactory.getDefaultQueue().add(options);
  }
}


