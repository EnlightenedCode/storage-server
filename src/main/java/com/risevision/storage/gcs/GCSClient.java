package com.risevision.storage.gcs;

import java.util.Arrays;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.storage.Storage;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.risevision.storage.Globals;

public class GCSClient {
  private static final UrlFetchTransport HTTP_TRANSPORT = new UrlFetchTransport();

  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

  private static final GoogleCredential credential;
  private static Storage storageClient;

  static {
    credential = new P12CredentialBuilder().getCredentialFromP12File
    (Globals.RVMEDIA_P12_PATH, Globals.RVMEDIA_ID, Globals.STORAGE_SCOPE);

    try {
      Storage storageClient = 
      new Storage.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(Globals.STORAGE_APP_NAME)
      .build();
    } catch (Exception e) {
      storageClient = null;
      throw e;
    }
  }

  public static Storage getStorageClient() {
    return storageClient;
  }
}
