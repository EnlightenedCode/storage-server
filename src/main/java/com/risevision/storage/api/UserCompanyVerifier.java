package com.risevision.storage.api;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.ServiceAccountAPIRequestor;
import com.risevision.storage.info.ServiceFailedException;

public class UserCompanyVerifier {
  private static final String HTTP_CHARSET = "UTF-8";
  private static final Logger log = Logger.getAnonymousLogger();

  private String replaceWithLocalUserEmail(String email) throws ServiceFailedException {
      if (Globals.devserver) {
          User localUser = UserServiceFactory.getUserService().getCurrentUser();
          if (localUser == null) {
              String loginURL = "http://localhost:8888/_ah/login?continue=%2f";
              log.warning("Local user not logged in. Log in at " + loginURL);
              throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
          }
          return localUser.getEmail();
      }else {
          return email;
      }
  }

  public void verifyUserCompany(String companyId, String email)
  throws ServiceFailedException {
    email = replaceWithLocalUserEmail(email);

    log.info("Verifying company access for user " + email);

    if (Globals.devserver) {return;}

    try {
      GenericUrl url = new GenericUrl
      (Globals.USER_VERIFICATION_URL
      .replace("COMPANYID", URLEncoder.encode(companyId, HTTP_CHARSET))
      .replace("EMAIL", URLEncoder.encode(email, HTTP_CHARSET)));

      HttpResponse response = ServiceAccountAPIRequestor.makeRequest
      (ServiceAccountAPIRequestor.SERVICE_ACCOUNT.CORE, "POST", url, null);

      if (!response.parseAsString().contains("\"allowedAccess\": true")) {
        throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }
}
