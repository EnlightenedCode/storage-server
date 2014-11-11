package com.risevision.storage.gcs;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.security.GeneralSecurityException;
import java.security.AccessControlException;

import com.risevision.storage.Globals;

public class LocalCredentialBuilder {
  protected static final Logger log = Logger.getAnonymousLogger();
  private static GoogleCredential credential;
  private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();
  private static File p12File;

  public static GoogleCredential getCredentialFromP12File() {
    if (credential != null) {return credential;}

    GoogleCredential.Builder builder = 
      new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
      .setJsonFactory(JSON_FACTORY)
      .setServiceAccountId(Globals.ACCESS_ID)
      .setServiceAccountScopes(Arrays.asList(Globals.STORAGE_SCOPE));

    try {
      File p12File = new File(Globals.LOCAL_P12_PATH);
      credential = builder.setServiceAccountPrivateKeyFromP12File(p12File)
                          .build();
    } catch (NullPointerException e) {
      log.warning("Could not access local p12 file - Null Pointer Exception");
    } catch (GeneralSecurityException e) {
      log.warning("Could not access local p12 file - Security violation");
    } catch (IOException e) {
      log.warning("Could not access local p12 file - IO error");
      File file = new File("./");
      log.info("Attempted path: " + Globals.LOCAL_P12_PATH);
      log.info("Current directory contents:");
      log.info(Arrays.toString(file.list()));
    }

    return credential;
  }
}
