package com.risevision.storage.servertasks;

import java.util.Map;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.reflect.Method;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

class AddPublicReadBucketServerTask extends ServerTask {
  AddPublicReadBucketServerTask
  (Storage gcsStorageClient, Map<String, String[]> params) {
    super(gcsStorageClient, params);
    verifyParams("bucket");
  }

  public void handleRequest() throws IOException {
    log.info("Adding public read on: " + requestParams.get("bucket")[0]);

    try {
      ObjectAccessControl acl = new ObjectAccessControl();
      acl.setEntity("allUsers").setRole("READER");

      Storage.ObjectAccessControls.Insert insertRequest = 
      gcsStorageClient.objectAccessControls().insert
      (requestParams.get("bucket")[0], "toBeIterated", acl);

      Storage.Objects.List listRequest = gcsStorageClient.objects().list
      (requestParams.get("bucket")[0])
      .setPrefix(null).setDelimiter(null);

 
      Method iterationRequestUpdater = null;
      try {
        iterationRequestUpdater =
        insertRequest.getClass().getMethod("setObject", String.class);

      } catch (Exception e) {
        throw new IOException("Method reflection error");
      }

      new BatchBucketObjectsUpdater<Storage.ObjectAccessControls.Insert>
      (insertRequest, iterationRequestUpdater, listRequest, requestParams)
      .beginUpdate();
    } catch (IOException e) {
      log.severe(e.toString());
      throw new IOException(e);
    }
  }
}

