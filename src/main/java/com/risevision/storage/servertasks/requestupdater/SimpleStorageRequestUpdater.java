package com.risevision.storage.servertasks.requestupdater;

import java.io.IOException;

import com.google.api.services.storage.StorageRequest;
import com.google.api.services.storage.model.Bucket;

public class SimpleStorageRequestUpdater implements StorageRequestUpdater {
  private StorageRequest<?> templateRequest;
  private String updateKey;

  public SimpleStorageRequestUpdater(StorageRequest<?> request) {
    templateRequest = request;
    updateKey = (request.getResponseClass() == Bucket.class ? "bucket" : "object"); 
  }

  public StorageRequest<?> provideUpdatedRequest(String objectName) throws IOException {
    return (StorageRequest<?>) templateRequest.clone().set(updateKey, objectName);
  }
}
