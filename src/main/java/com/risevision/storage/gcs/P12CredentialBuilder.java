package com.risevision.storage.gcs;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.risevision.storage.Globals;

public class P12CredentialBuilder {
  private static final Logger log = Logger.getAnonymousLogger();
  private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
  private static final UrlFetchTransport
                       urlTransport = UrlFetchTransport.getDefaultInstance();

  private GoogleCredential credential;
  private GoogleCredential.Builder builder;

  public P12CredentialBuilder() {
    builder = new GoogleCredential.Builder()
    .setTransport(urlTransport)
    .setJsonFactory(jsonFactory);
  }

  public GoogleCredential getCredentialFromP12File
  (String p12path, String id, String scope) {
    builder.setServiceAccountId(id).setServiceAccountScopes(Arrays.asList(scope));

    try {
      File p12File = new File(p12path);
      credential = builder.setServiceAccountPrivateKeyFromP12File(p12File).build();
      credential.refreshToken();
    } catch (Exception e) {
      log.warning("Error building credential");
      e.printStackTrace();
    }

    return credential;
  }
}
