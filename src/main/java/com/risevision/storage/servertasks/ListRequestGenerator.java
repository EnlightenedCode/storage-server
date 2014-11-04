package com.risevision.storage.servertasks;

import java.io.IOException;

import com.google.api.services.storage.model.*;
import com.google.api.services.storage.*;

import com.risevision.storage.Globals;

class ListRequestGenerator {
  private StorageRequest listRequest;

  ListRequestGenerator(Storage gcsStorageClient) throws IOException {
      listRequest = gcsStorageClient.buckets().list(Globals.PROJECT_ID);
  }

  ListRequestGenerator(Storage gcsStorageClient, String bucket) throws IOException {
      listRequest = gcsStorageClient.objects().list(bucket);
  }

  StorageRequest getRequest() {
    return listRequest;
  }
}
