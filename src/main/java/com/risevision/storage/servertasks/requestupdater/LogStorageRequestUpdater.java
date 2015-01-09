package com.risevision.storage.servertasks.requestupdater;

import java.io.IOException;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageRequest;
import com.google.api.services.storage.model.Bucket;
import com.risevision.storage.Globals;

public class LogStorageRequestUpdater implements StorageRequestUpdater {
  private Storage gcsClient;
  
  public LogStorageRequestUpdater(Storage gcsClient) {
    this.gcsClient = gcsClient;
  }
  
  @Override
  public StorageRequest<?> provideUpdatedRequest(String objectName) throws IOException {
    Bucket bucket = new Bucket()
    .setLogging(new Bucket.Logging()
    .setLogBucket(Globals.LOGS_BUCKET_NAME)
    .setLogObjectPrefix(objectName));

    return gcsClient.buckets().patch(objectName, bucket);
  }
}
