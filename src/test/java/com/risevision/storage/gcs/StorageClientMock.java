package com.risevision.storage.gcs;

import java.io.IOException;

import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.http.HttpTransport;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects.Insert;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.client.http.*;
import com.google.api.client.json.*;

public class StorageClientMock extends Storage {
  private Objects.Insert objectInsert;
  private StorageObject executionResult;
  private IOException exception;

  public StorageClientMock() {
    super(new MockHttpTransport()
         ,new MockJsonFactory()
         ,new HttpRequestInitializer(){public void initialize(HttpRequest req){}});
    executionResult = new StorageObject();
  }

  public void setExecutionResult(StorageObject result) {
    executionResult = result;
  }

  public void setException(IOException exception) {
    this.exception = exception;
  }

  public Objects.Insert getObjectsInsert() {return objectInsert;}

  public Objects objects() {return new Objects();}

  class Objects extends Storage.Objects {
    public StorageClientMock.Objects.Insert insert
      (String bucketName,
       StorageObject data,
       AbstractInputStreamContent mediaContent) {
        objectInsert = new Insert(bucketName, data);
        return objectInsert;
      }

    public class Insert extends Storage.Objects.Insert { 
      private String bucketName;
      private StorageObject data;

      public Insert(String bucketName, StorageObject data) {
        super(bucketName, data);
        this.bucketName = bucketName;
        this.data = data;
      }

      public StorageObject execute() throws IOException {
        if (exception != null) {
          throw exception;
        }
        
        return executionResult;
      }

      public String getBucketName() {return bucketName;}
      public StorageObject getData() {return data;}
    }
  }
}
