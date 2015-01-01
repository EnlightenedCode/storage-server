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
    listResult = new GenericData()
                .set("kind", "storage#buckets")
                .set("items", ImmutableList.of(new GenericData().set("kind", "storage#bucket")
                                                                .set("name", "risemedialibrary-915411be-5529-44b6-926f-d2bab9af66f0")));
  }

  void setupDefaultAcl() throws IOException {
    Bucket patchBucket = new Bucket()
                      .setDefaultObjectAcl(ImmutableList.of(
      new ObjectAccessControl().setEntity("allUsers")
                               .setRole("READER"),
      new ObjectAccessControl().setEntity("project-viewers-" + Globals.PROJECT_ID)
                               .setRole("READER"),
      new ObjectAccessControl().setEntity("project-owners-" + Globals.PROJECT_ID)
                               .setRole("OWNER"),
      new ObjectAccessControl().setEntity("project-editors-" + Globals.PROJECT_ID)
                               .setRole("OWNER")));
    iteratingRequest = 
    gcsClient.buckets().patch("toBeIterated", patchBucket);
  }
}

