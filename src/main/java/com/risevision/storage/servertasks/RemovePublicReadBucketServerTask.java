package com.risevision.storage.servertasks;

import java.util.Map;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.reflect.Method;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.Storage;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

class RemovePublicReadBucketServerTask extends ServerTask {
  RemovePublicReadBucketServerTask
  (Storage gcsStorageClient, Map<String, String[]> params) {
    super(gcsStorageClient, params);
    verifyParams("bucket");
  }

  public void handleRequest() throws IOException {
    log.info("Removing public read on: " + requestParams.get("bucket")[0]);

    try {
      Storage.ObjectAccessControls.Delete deleteRequest = 
      gcsStorageClient.objectAccessControls().delete
      (requestParams.get("bucket")[0], "toBeIterated", "allUsers");

      Storage.Objects.List listRequest = gcsStorageClient.objects().list
      (requestParams.get("bucket")[0])
      .setPrefix(null).setDelimiter(null);

 
      Method iterationRequestUpdater = null;
      try {
        iterationRequestUpdater =
        deleteRequest.getClass().getMethod("setObject", String.class);

      } catch (Exception e) {
        throw new IOException("Method reflection error");
      }
      new BatchBucketObjectsUpdater<Storage.ObjectAccessControls.Delete>
      (deleteRequest, iterationRequestUpdater, listRequest, requestParams)
      .beginUpdate();
    } catch (IOException e) {
      log.severe(e.toString());
      throw new IOException(e);
    }
  }
}


