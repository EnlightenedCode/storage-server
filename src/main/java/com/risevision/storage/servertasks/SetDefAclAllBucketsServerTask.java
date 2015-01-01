package com.risevision.storage.servertasks;

import java.util.Map;
import java.io.IOException;

import com.risevision.storage.Globals;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;
import com.google.common.collect.ImmutableList;

class SetDefAclAllBucketsServerTask extends BatchServerTask {

  SetDefAclAllBucketsServerTask
  (Storage gcsStorageClient, Map<String, String[]> params) throws IOException {
    super(gcsStorageClient, params);
  }

  public void handleRequest() throws IOException {
    log.info("Setting default acl on all buckets");

    setupDefaultAcl();
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

  void setupDefaultAcl() throws IOException {
    Bucket patchBucket = new Bucket();
    patchBucket.setDefaultObjectAcl(ObjectAclFactory.getDefaultAcl());

    iteratingRequest =  gcsClient.buckets().patch("toBeIterated", patchBucket);
  }
}

