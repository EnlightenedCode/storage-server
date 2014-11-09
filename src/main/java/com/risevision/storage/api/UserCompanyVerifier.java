package com.risevision.storage.api;

import com.risevision.storage.Globals;
import com.risevision.storage.info.ServiceFailedException;

import java.net.*;
import java.io.*;
import java.util.logging.Logger;

class UserCompanyVerifier {
  private static final String HTTP_CHARSET = "UTF-8";
  private static final Logger log = Logger.getAnonymousLogger();
  private static HttpURLConnection httpConn;
  private static URL url;

  private UserCompanyVerifier() {}

  static {
    try {
      URL url = new URL(Globals.USER_VERIFICATION_URL);
      HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
      httpConn.setConnectTimeout(9000);
      httpConn.setReadTimeout(9000);
      httpConn.setInstanceFollowRedirects(false);
      httpConn.setDoOutput(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static BufferedReader reader;
  static OutputStream output;

  static void verifyUserCompany(String companyId, String email)
  throws ServiceFailedException {
    if (Globals.devserver) {
      email = com.google.appengine.api.users.UserServiceFactory.
              getUserService().getCurrentUser().getEmail();
    }

    log.info("Verifying company access for user " + email);
    String result = "";
    String line;

    try {
      String query = String.format("username=%s&companyId=%s", 
                                   URLEncoder.encode(email, HTTP_CHARSET), 
                                   URLEncoder.encode(companyId, HTTP_CHARSET));

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
}
