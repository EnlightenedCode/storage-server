package com.risevision.storage.servertasks;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;
import com.google.api.client.googleapis.services.AbstractGoogleClient;

class StorageRequestUpdater {
  private StorageRequest templateRequest;
  private String updateKey;

  StorageRequestUpdater(StorageRequest request) {
    templateRequest = request;
    updateKey = (request.getResponseClass() == Bucket.class ? "bucket" : "object"); 
  }

  public StorageRequest provideUpdatedRequest(String objectName) {
    return (StorageRequest)templateRequest.clone().set(updateKey, objectName);
  }
}
