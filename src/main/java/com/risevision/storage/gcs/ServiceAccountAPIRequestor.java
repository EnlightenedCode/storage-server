package com.risevision.storage.gcs;

import java.io.IOException;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.*;
import com.risevision.storage.Globals;

public class ServiceAccountAPIRequestor {
  public enum SERVICE_ACCOUNT {
    CORE(Globals.RVCORE_ID, Globals.RVCORE_P12_PATH),
    MEDIA_LIBRARY(Globals.RVMEDIA_ID, Globals.RVMEDIA_P12_PATH);

    private String id;
    private String p12_path;
    private GoogleCredential credential;
    private HttpRequestFactory requestFactory;
    private HttpRequestInitializer requestInitializer;

    SERVICE_ACCOUNT(String id, String p12_path) {
      this.id = id;
      this.p12_path = p12_path;

      this.credential = new P12CredentialBuilder().getCredentialFromP12File
      (p12_path, id, Globals.EMAIL_SCOPE);

      this.requestInitializer = new HttpRequestInitializer() {
        public void initialize(HttpRequest request) throws IOException {
          request.getHeaders().setAuthorization(credential.getAccessToken());
        }
      };

      this.requestFactory = 
      new UrlFetchTransport().createRequestFactory(this.requestInitializer);
    }

    public GoogleCredential getCredential() {return credential;}
  }

  public static HttpResponse makeRequest
  (SERVICE_ACCOUNT serviceAccount, String method, GenericUrl url, HttpContent content)
  throws IOException {
    if (serviceAccount.getCredential().getExpirationTimeMilliseconds() < 60000) {
      serviceAccount.getCredential().refreshToken();
    }

    return serviceAccount.requestFactory.buildRequest(method, url, content).execute();
  }
}

