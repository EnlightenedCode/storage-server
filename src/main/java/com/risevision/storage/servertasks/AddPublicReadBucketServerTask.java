package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;

public class AddPublicReadBucketServerTask extends BatchServerTask {
  String bucketName;

  public AddPublicReadBucketServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
    confirmURLParams("bucket");
    bucketName = requestParams.get("bucket")[0];
  }

  public void handleRequest() throws IOException {
    log.info("Adding public read on: " + bucketName);

    setupACLInsertRequest();

    listRequest = new ListRequestGenerator(gcsClient, bucketName).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    listResult = (GenericData) listRequest.execute();

    prepareBatchRequest();
    submitBatchRequest();

    submitNextTask();
  }

  private void setupACLInsertRequest() throws IOException {
    ObjectAccessControl acl = new ObjectAccessControl();
    acl.setEntity("allUsers").setRole("READER");

    iteratingRequest = 
    gcsClient.objectAccessControls().insert
    (requestParams.get("bucket")[0], "toBeIterated", acl);
  }
}

