package com.risevision.storage.servertasks;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.util.GenericData;
import com.risevision.storage.Globals;

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
    listResult = filterTrashItems(listResult);

    prepareBatchRequest();
    submitBatchRequest();

    submitNextTask();
  }
  
  private GenericData filterTrashItems(GenericData listResult) {
    GenericData returnResult = new GenericData();
    returnResult.set("kind", listResult.get("kind"));
    returnResult.set("nextPageToken", listResult.get("nextPageToken"));
    returnResult.set("prefixes", listResult.get("prefixes"));

    List<GenericData> returnItems = new ArrayList<>();
    for (GenericData item : (List<GenericData>) listResult.get("items")) {
      String name = (String) item.get("name");
      if (name != null && !name.startsWith(Globals.TRASH)) {
        returnItems.add(item);
      }
    }
    returnResult.set("items", returnItems);
    return returnResult;
  }

  private void setupACLInsertRequest() throws IOException {
    ObjectAccessControl acl = new ObjectAccessControl();
    acl.setEntity("allUsers").setRole("READER");

    iteratingRequest = 
    gcsClient.objectAccessControls().insert
    (requestParams.get("bucket")[0], "toBeIterated", acl);
  }
}
