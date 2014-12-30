package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.Map;

import org.mortbay.log.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.risevision.storage.Globals;

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
    
    if(requestParams.get("object")[0].startsWith(Globals.TRASH)) {
      Log.info("Item " + requestParams.get("object")[0] + " ignored. Trash items should not be public.");
      return;
    }

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
