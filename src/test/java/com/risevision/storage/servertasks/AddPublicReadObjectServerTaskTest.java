package com.risevision.storage.servertasks;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.risevision.storage.gcs.GCSMockClientBuilder;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;

public class AddPublicReadObjectServerTaskTest {

  Storage mockGCSClient = new GCSMockClientBuilder(200).build();
  Map<String, String[]> requestParams = new HashMap<>();

  @Test (expected = IllegalArgumentException.class)
  public void itThrowsWhenNoBucketSpecified() throws IOException {
    requestParams.put("object", new String[] {"fileName"});
    AddPublicReadObjectServerTask task = new AddPublicReadObjectServerTask
    (mockGCSClient, requestParams);
  }

  @Test (expected = IllegalArgumentException.class)
  public void itThrowsWhenNoObjectSpecified() throws IOException {
    requestParams.put("bucket", new String[] {"bucketName"});
    AddPublicReadObjectServerTask task = new AddPublicReadObjectServerTask
    (mockGCSClient, requestParams);
  }

  @Test (expected = IOException.class)
  public void itThrowsOnServerError() throws IOException {
    requestParams.put("object", new String[] {"fileName"});
    requestParams.put("bucket", new String[] {"bucketName"});

    AddPublicReadObjectServerTask task = new AddPublicReadObjectServerTask
    (new GCSMockClientBuilder(404).build(), requestParams);

    task.handleRequest();
  }

  @Test public void itReturnsOnSuccess() throws IOException {
    requestParams.put("object", new String[] {"fileName"});
    requestParams.put("bucket", new String[] {"bucketName"});

    AddPublicReadObjectServerTask task = new AddPublicReadObjectServerTask
    (mockGCSClient, requestParams);

    task.handleRequest();
  }
}

