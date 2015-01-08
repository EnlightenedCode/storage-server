package com.risevision.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.appengine.api.utils.SystemProperty;

public final class Globals {
  public static final Boolean devserver = 
    SystemProperty.environment.value() == 
    SystemProperty.Environment.Value.Development;

  public static final String STORAGE_SCOPE =
    "https://www.googleapis.com/auth/devstorage.full_control";
  public static final String EMAIL_SCOPE =
    "https://www.googleapis.com/auth/userinfo.email";
  public static final String BQ_SCOPE =
    "https://www.googleapis.com/auth/bigquery";

  public static final String COMPANY_BUCKET_PREFIX =
    "risemedialibrary-";
          
  public static final String STORE_PRODUCT_CODE =
    "b0cba08a4baa0c62b8cdc621b6f6a124f89a03db";

  public static final String STORE_CLIENT_ID = 
    "614513768474.apps.googleusercontent.com";

  public static final String LOGGING_ENABLED_XML = "<Logging>"
                      + "<LogBucket>%logBucket%</LogBucket>"
                      + "<LogObjectPrefix>%bucketName%</LogObjectPrefix>"
                      + "</Logging>";

  public static final String RESUMABLE_UPLOAD_REQUEST_URI =
    "https://www.googleapis.com/upload/storage/v1/b/myBucket/o?uploadType=resumable&name=";

  public static final String DELETED_NAMESPACE =
    "-deleted-";

  public static final String UNKNOWN_USER =
    "unknown";

  public static final String HQ_TIMEZONE = 
    "America/Toronto";

  public static final String TRASH = "--TRASH--/";
  
  public static final String LOGS_BUCKET_NAME;
  public static final String DATASET_ID;
  public static final String STORE_BASE_URL;
  public static final String USER_VERIFICATION_URL;
  public static final String RVCORE_P12_PATH;
  public static final String RVCORE_ID;
  public static final String PROJECT_ID;
  public static final String STORAGE_APP_NAME;
  public static final String RVMEDIA_P12_PATH;
  public static final String RVMEDIA_ID;

  private static String bucketParameter;
  private static String datasetParameter;
  private static String storeBaseURL;
  private static String userVerificationURL;
  private static String rvcore_p12_path;
  private static String rvcore_id;
  private static String project_id;
  private static String storage_app_name;
  private static String rvmedia_p12_path;
  private static String rvmedia_id;

  static {
    Properties buildProperties = new Properties();
    InputStream fileData = null;

    try {
      fileData = Globals.class.getResourceAsStream("/build.properties");
      buildProperties.load(fileData);

      bucketParameter = buildProperties.getProperty("LOGS_BUCKET_NAME");
      datasetParameter = buildProperties.getProperty("DATASET_ID");
      storeBaseURL = buildProperties.getProperty("STORE_BASE_URL");
      userVerificationURL = buildProperties.getProperty("USER_VERIFICATION_URL");
      rvcore_p12_path = buildProperties.getProperty("RVCORE_P12_PATH");
      rvcore_id = buildProperties.getProperty("RVCORE_ID");
      project_id = buildProperties.getProperty("PROJECT_ID");
      storage_app_name = buildProperties.getProperty("STORAGE_APP_NAME");
      rvmedia_p12_path = buildProperties.getProperty("RVMEDIA_P12_PATH");
      rvmedia_id = buildProperties.getProperty("RVMEDIA_ID");
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      LOGS_BUCKET_NAME = bucketParameter;
      DATASET_ID = datasetParameter;
      STORE_BASE_URL = storeBaseURL;
      USER_VERIFICATION_URL = userVerificationURL;
      RVCORE_P12_PATH = rvcore_p12_path;
      RVCORE_ID= rvcore_id;
      PROJECT_ID = project_id;
      STORAGE_APP_NAME = storage_app_name;
      RVMEDIA_ID = rvmedia_id;
      RVMEDIA_P12_PATH = rvmedia_p12_path;

      if (fileData != null) {
        try {
          fileData.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static final String SUBSCRIPTION_AUTH_URL =
    STORE_BASE_URL + "/v1/widget/auth?pc=" +
    STORE_PRODUCT_CODE + "&cid=";

  public static final String SUBSCRIPTION_STATUS_URL =
    STORE_BASE_URL + "/v1/company/companyId/product/status?pc=" +
    STORE_PRODUCT_CODE;
}
