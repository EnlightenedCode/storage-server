package com.risevision.storage.gcs;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

import org.apache.commons.codec.binary.Base64;

import com.risevision.storage.Globals;

public class LocalSignedURIGenerator {
  private static PrivateKey key;
  
  static {
    try {
      key = loadKeyFromPkcs12(Globals.RVCORE_P12_PATH, "notasecret".toCharArray());
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
  }
  
  private LocalSignedURIGenerator() {}
  
  public static String getSignedURI(String verb, String bucketName, String objectName) throws Exception {
    return generateSigningURL(verb, bucketName, objectName);
  }
  
  private static String generateSigningURL(String verb, String bucketName, String objectName) throws Exception {
    long expiration = getExpiration();
    String url_signature = signString(verb + "\n\n\n" + expiration + "\n" + "/" + bucketName + "/" + objectName);
    String signed_url = "https://storage.googleapis.com/" + bucketName + "/"
        + objectName + "?GoogleAccessId=" + Globals.RVCORE_ID
        + "&Expires=" + expiration + "&Signature="
        + URLEncoder.encode(url_signature, "UTF-8");
    return signed_url;
  }

  private static PrivateKey loadKeyFromPkcs12(String filename, char[] password) throws Exception {
    FileInputStream fis = new FileInputStream(filename);
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(fis, password);
    return (PrivateKey) ks.getKey("privatekey", password);
  }

  private static String signString(String stringToSign) throws Exception {
    if (key == null) {
      throw new Exception("Private Key not initalized");
    }
    
    Signature signer = Signature.getInstance("SHA256withRSA");
    signer.initSign(key);
    signer.update(stringToSign.getBytes("UTF-8"));
    byte[] rawSignature = signer.sign();
    return new String(Base64.encodeBase64(rawSignature, false), "UTF-8");
  }
  
  private static long getExpiration() {
    return System.currentTimeMillis() / 1000 + 60;
  }
}
