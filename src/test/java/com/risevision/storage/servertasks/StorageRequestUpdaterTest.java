package com.risevision.storage.servertasks;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;

public class StorageRequestUpdaterTest {

  Storage mockClient = new GCSMockClientBuilder(200).build();

  @Test
  public void itProvidesAnUpdatedBucketRequest() throws IOException {
    Storage.Buckets.Patch request;
    request = mockClient.buckets().patch("templateBucketName", null);

    StorageRequestUpdater updater;
    updater = new StorageRequestUpdater(request);

    StorageRequest newReq;
    newReq = updater.provideUpdatedRequest("updatedBucketName");

    assertThat("the request target was updated"
              ,(String)newReq.get("bucket")
              ,equalTo("updatedBucketName"));
  }

  @Test
  public void itProvidesAnUpdatedObjectRequest() throws IOException {
    Storage.Objects.Patch request;
    request = mockClient.objects().patch("templateBucketName", "objName", null);

    StorageRequestUpdater updater;
    updater = new StorageRequestUpdater(request);

    StorageRequest newReq;
    newReq = updater.provideUpdatedRequest("updatedObjectName");

    assertThat("the request target was updated"
              ,(String)newReq.get("object")
              ,equalTo("updatedObjectName"));
  }
}
