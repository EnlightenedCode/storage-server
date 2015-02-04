package com.risevision.storage.gcs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

import com.google.appengine.api.appidentity.*;

public class AppEngineCredentialSignedURIGenerator {
  private static final AppIdentityService identityService = 
  AppIdentityServiceFactory.getAppIdentityService();
  
  private AppEngineCredentialSignedURIGenerator() {}
  
  public static String getSignedURI
  (String verb, String bucketName, String objectName)
  throws UnsupportedEncodingException {
    long expiration = getExpiration();

    String url_signature = signString
    (verb + "\n\n\n" + expiration + "\n" + "/" + bucketName + "/" + objectName);

    String signedURL = "https://storage.googleapis.com/" + bucketName + "/"
    + objectName + "?GoogleAccessId=" + identityService.getServiceAccountName()
    + "&Expires=" + expiration + "&Signature="
    + URLEncoder.encode(url_signature, "UTF-8");

    return signedURL;
  }

  private static String signString(String stringToSign)
  throws UnsupportedEncodingException {
    AppIdentityService.SigningResult signingResult = identityService.signForApp(stringToSign.getBytes());
    return new String(Base64.encodeBase64(signingResult.getSignature(), false), "UTF-8");
  }
  
  private static long getExpiration() {
    return System.currentTimeMillis() / 1000 + 60;
  }
}
