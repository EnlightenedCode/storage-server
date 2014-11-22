package com.risevision.storage.api;

import com.risevision.storage.Globals;
import com.risevision.storage.gcs.LocalCredentialBuilder;
import com.risevision.storage.info.ServiceFailedException;

import java.net.URLEncoder;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.appengine.api.users.*;

class UserCompanyVerifier {
  private static final String HTTP_CHARSET = "UTF-8";
  private static final Logger log = Logger.getAnonymousLogger();
  private static GoogleCredential 
  credential = new LocalCredentialBuilder().getCredentialFromP12File
               (Globals.RVCORE_P12_PATH, Globals.RVCORE_ID, Globals.EMAIL_SCOPE);
  private static HttpRequestFactory
  httprequestFactory = new UrlFetchTransport().createRequestFactory();

  void verifyUserCompany(String companyId, String email)
  throws ServiceFailedException {
    if (Globals.devserver) {
      User localUser = UserServiceFactory.getUserService().getCurrentUser();
      if (localUser == null) {
        String loginURL = "http://localhost:8888/_ah/login?continue=%2f";
        log.warning("Local user not logged in. Log in at " + loginURL);
        throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
      }
      email = localUser.getEmail();
    }

    log.info("Verifying company access for user " + email);

    try {
      GenericUrl url = new GenericUrl
      (Globals.USER_VERIFICATION_URL
      .replace("COMPANYID", URLEncoder.encode(companyId, HTTP_CHARSET))
      .replace("EMAIL", URLEncoder.encode(email, HTTP_CHARSET)));

      HttpRequest request = httprequestFactory.buildPostRequest(url, null);
      if (credential.getExpirationTimeMilliseconds() < 60000) {credential.refreshToken();}
      request.setInterceptor(credential);

      if (!request.execute().parseAsString().contains("\"allowedAccess\": true")) {
        throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }
}
