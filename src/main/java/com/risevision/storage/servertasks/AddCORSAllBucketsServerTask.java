package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;
import com.google.common.collect.ImmutableList;

class AddCORSAllBucketsServerTask extends BatchServerTask {
  String bucketName;

  AddCORSAllBucketsServerTask
  (Storage gcsStorageClient, Map<String, String[]> params) throws IOException {
    super(gcsStorageClient, params);
    confirmURLParams("bucket");
    bucketName = requestParams.get("bucket")[0];
  }

  public void handleRequest() throws IOException {
    log.info("Adding CORS to all buckets");

    setupCORSPatchRequest();
    setListResult();
    prepareBatchRequest();
    submitBatchRequest();
    submitNextTask();
  }

  void setListResult() throws IOException {
    listRequest = new ListRequestGenerator(gcsClient).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    listResult = (GenericData) listRequest.execute();
  }

  void setupCORSPatchRequest() throws IOException {
    Bucket.Cors cors = new Bucket.Cors();
    cors.setMaxAgeSeconds(3600)
      .setMethod(ImmutableList.of("GET", "PUT", "POST"))
      .setOrigin(ImmutableList.of("*"))
      .setResponseHeader(ImmutableList.of(
            "Content-Type", "x-goog-resumable", "Content-Length"));

    Bucket bucket = new Bucket().setCors(ImmutableList.of(cors));

    iteratingRequest = 
    gcsClient.buckets().patch("toBeIterated", bucket);
  }
}
