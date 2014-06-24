package com.risevision.storage.gcs;

import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;

import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.services.storage.Storage.Buckets.*;
import com.google.api.services.storage.Storage.Objects.*;
import com.google.api.client.http.ByteArrayContent;
import com.risevision.storage.Globals;

public class GCSClient {
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

  public static StorageObject getStorageObject() {
    return new StorageObject();
  }

  public static Storage getStorageClient(HttpRequestInitializer credential) {
    Storage storage = 
      new Storage.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                 .setApplicationName(Globals.STORAGE_APP_NAME)
                 .build();
    return storage;
  }
}
