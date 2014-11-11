package com.risevision.storage.api;

import com.risevision.storage.Globals;
import com.risevision.storage.gcs.LocalCredentialBuilder;
import com.risevision.storage.info.ServiceFailedException;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.net.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.Arrays;

class UserCompanyVerifier {
  private static final String HTTP_CHARSET = "UTF-8";
  private static final Logger log = Logger.getAnonymousLogger();
  private GoogleCredential credential;
  private static URL url;

  BufferedReader reader;
  OutputStream output;

  static {
    try {
      url = new URL(Globals.USER_VERIFICATION_URL);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void verifyUserCompany(String companyId, String email)
  throws ServiceFailedException {
    if (Globals.devserver) {
      email = com.google.appengine.api.users.UserServiceFactory.
              getUserService().getCurrentUser().getEmail();
    }

    log.info("Verifying company access for user " + email);
    String result = "";
    String line;

    try {
      HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
      httpConn.setConnectTimeout(9000);
      httpConn.setReadTimeout(9000);
      httpConn.setInstanceFollowRedirects(false);
      httpConn.setDoOutput(true);
      String query = String.format("username=%s&companyId=%s", 
                                   URLEncoder.encode(email, HTTP_CHARSET), 
                                   URLEncoder.encode(companyId, HTTP_CHARSET));

      httpConn.setRequestProperty("Authorization", "Bearer " + getToken()); 
      output = httpConn.getOutputStream();
      output.write(query.getBytes(HTTP_CHARSET));
      output.close();

      reader = new BufferedReader(
                              new InputStreamReader(httpConn.getInputStream()));

      line = reader.readLine();
      while (line != null) {
        result += line;
        line = reader.readLine();
      }

      reader.close();

      log.info("User Verification:" + result);
      if (!result.contains("\"allowedAccess\": true")) {
        throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    } finally {
      try {
        if (reader != null) {reader.close();}
        if (output != null) {output.close();}
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String getToken() throws IOException {
    if (credential == null) {
      credential = LocalCredentialBuilder.getCredentialFromP12File
      (Globals.RVCORE_P12_PATH, Globals.RVCORE_ID, Globals.EMAIL_SCOPE);

      credential.refreshToken();
    } else {
      if (credential.getExpiresInSeconds() < 50) {credential.refreshToken();}
    }
    return credential.getAccessToken();
  }
}
