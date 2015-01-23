package com.risevision.storage.api;

import java.io.IOException;
import java.net.URLEncoder;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.ServiceAccountAPIRequestor;
import com.risevision.storage.info.ServiceFailedException;

public class UserCompanyVerifier extends AbstractVerifier {
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
