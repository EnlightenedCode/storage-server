package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;
import com.risevision.storage.ObjectAclFactory;

public class SetObjectAclsToDefaultServerTask extends BatchServerTask {
  String bucketName;

  public SetObjectAclsToDefaultServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
    confirmURLParams("bucket");
    bucketName = requestParams.get("bucket")[0];
  }

  public void handleRequest() throws IOException {
    log.info("Setting object ACLs on: " + bucketName);

    setupObjectPatch();

    listRequest = new ListRequestGenerator(gcsClient, bucketName).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    listResult = (GenericData) listRequest.execute();

    prepareBatchRequest();
    submitBatchRequest();

    submitNextTask();
  }

  private void setupObjectPatch() throws IOException {
    StorageObject object = new StorageObject();
    object.setAcl(ObjectAclFactory.getDefaultAcl());

    iteratingRequest = 
    gcsClient.objects().patch
    (requestParams.get("bucket")[0], "toBeIterated", object);
  }
}


