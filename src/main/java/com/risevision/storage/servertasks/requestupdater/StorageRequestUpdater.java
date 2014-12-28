package com.risevision.storage.servertasks.requestupdater;

import java.io.IOException;

import com.google.api.services.storage.StorageRequest;

public interface StorageRequestUpdater {
  public StorageRequest<?> provideUpdatedRequest(String objectName) throws IOException;
}
