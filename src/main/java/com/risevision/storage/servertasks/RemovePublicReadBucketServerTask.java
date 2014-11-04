package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;

class RemovePublicReadBucketServerTask extends BatchServerTask {
  String bucketName;

  RemovePublicReadBucketServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
    confirmURLParams("bucket");
    bucketName = requestParams.get("bucket")[0];
  }

  public void handleRequest() throws IOException {
    log.info("Removing public read on: " + bucketName);

    setupACLDeleteRequest();

    listRequest = new ListRequestGenerator(gcsClient, bucketName).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    listResult = (GenericData) listRequest.execute();

    prepareBatchRequest();
    submitBatchRequest();

    submitNextTask();
  }

  private void setupACLDeleteRequest() throws IOException {
    iteratingRequest = 
    gcsClient.objectAccessControls().delete
    (bucketName, "toBeIterated", "allUsers");
  }
}

