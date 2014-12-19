package com.risevision.storage.servertasks;

import com.google.api.services.storage.*;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import com.google.api.client.googleapis.batch.*;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import java.util.logging.Logger;

class BatchRequestGenerator {
  private static final Logger log = Logger.getAnonymousLogger();
  private static final String REQUEST_ITEM_NAME_KEY = "name";

  private List<GenericData> items; 
  private ArrayList<GoogleJsonError> failures;
  private BatchRequest batchRequest;
  private StorageRequestUpdater requestUpdater;

  public BatchRequestGenerator
  (Storage gcsClient, StorageRequestUpdater requestUpdater, List<GenericData> items) {
    this.failures= new ArrayList<GoogleJsonError>();
    this.items = items;
    this.batchRequest = gcsClient.batch();
    this.requestUpdater = requestUpdater;
  }

  public BatchRequest generateBatch() throws IOException {
    JsonBatchCallback batchCallback = new JsonBatchCallback<GenericJson>(){
      public void onSuccess(GenericJson resp, HttpHeaders responseHeaders) {
        log.info("Batch task success");
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        log.warning(e.getMessage());
        failures.add(e);
      }
    };
    
    if(items != null) {
      for (GenericData item : items) {
        StorageRequest iterationRequest = requestUpdater
        .provideUpdatedRequest((String) item.get(REQUEST_ITEM_NAME_KEY));
  
        iterationRequest.queue
        (batchRequest, GoogleJsonErrorContainer.class, batchCallback);
  
        log.info("Added " + iterationRequest.get("bucket") + "/"
                 + item.get(REQUEST_ITEM_NAME_KEY) + " to batch");
      }
    }

    return batchRequest;
  }
}
