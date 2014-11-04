package com.risevision.storage.servertasks;

import java.util.Map;
import java.lang.IllegalArgumentException;
import java.io.IOException;

import com.google.api.services.storage.Storage;

class RemovePublicReadObjectServerTask extends ServerTask {
  RemovePublicReadObjectServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
    confirmURLParams("bucket", "object");
  }

  public void handleRequest() throws IOException {
    log.info("Removing public read on: " +
    requestParams.get("bucket")[0] + "/" +
    requestParams.get("object")[0]);

    gcsClient.objectAccessControls().delete
    (requestParams.get("bucket")[0], requestParams.get("object")[0], "allUsers")
    .execute();
  }
}

