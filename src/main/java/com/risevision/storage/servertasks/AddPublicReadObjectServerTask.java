package com.risevision.storage.servertasks;

import java.util.Map;
import java.lang.IllegalArgumentException;
import java.io.IOException;

import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.Storage;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

class AddPublicReadObjectServerTask extends ServerTask {
  AddPublicReadObjectServerTask
  (Storage gcsClient, Map<String, String[]> params) throws IOException {
    super(gcsClient, params);
    confirmURLParams("bucket", "object");
  }

  public void handleRequest() throws IOException {
    log.info("Adding public read on: " +
    requestParams.get("bucket")[0] + "/" +
    requestParams.get("object")[0]);

    ObjectAccessControl acl = new ObjectAccessControl();
    acl.setEntity("allUsers").setRole("READER");

    try {
      gcsClient.objectAccessControls().insert
      (requestParams.get("bucket")[0], requestParams.get("object")[0], acl)
      .execute();
    } catch (GoogleJsonResponseException e) {
      log.severe(e.getStatusMessage());
      throw new IOException(e);
    }
  }
}
