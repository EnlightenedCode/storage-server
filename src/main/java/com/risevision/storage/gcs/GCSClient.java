package com.risevision.storage.gcs;

import java.util.Arrays;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.storage.Storage;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.risevision.storage.Globals;

public class GCSClient {
  private static final UrlFetchTransport HTTP_TRANSPORT = new UrlFetchTransport();

  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

  private static final HttpRequestInitializer credential;

  static {
    if (Globals.devserver) {
      credential = new LocalCredentialBuilder().getCredentialFromP12File
      (Globals.RVMEDIA_P12_PATH, Globals.RVMEDIA_ID, Globals.STORAGE_SCOPE);
    } else {
      credential = new AppIdentityCredential(Arrays.asList(Globals.STORAGE_SCOPE));
    }
  }

  public static Storage getStorageClient() {
    Storage storage = 
      new Storage.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                 .setApplicationName(Globals.STORAGE_APP_NAME)
                 .build();
    return storage;
  }
}
