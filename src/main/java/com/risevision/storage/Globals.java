package com.risevision.storage;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Globals {

  public static final String BUCKET_NAME_PREFIX =
    "risemedialibrary-";
          
  public static final String PROJECT_ID =
    "452091732215";
  public static final String ACCESS_ID =
    "452091732215@developer.gserviceaccount.com";

  public static final String STORE_CLIENT_ID = 
    "614513768474.apps.googleusercontent.com";

  public static final String DELETED_NAMESPACE =
    "-deleted-";

  public static final String UNKNOWN_USER =
    "unknown";

  public static final String HQ_TIMEZONE = 
    "America/Toronto";

  public static final String LOGGING_ENABLED_XML; 
  public static final String LOGS_BUCKET_NAME;
  public static final String DATASET_ID;

  private static String loggingParameter;
  private static String bucketParameter;
  private static String datasetParameter;

  static {
    Properties buildProperties = new Properties();
    InputStream fileData = null;

    try {
      fileData = Globals.class.getResourceAsStream("/build.properties");
      buildProperties.load(fileData);

      loggingParameter = buildProperties.getProperty("LOGGING_ENABLED_XML");
      bucketParameter = buildProperties.getProperty("LOGS_BUCKET_NAME");
      datasetParameter = buildProperties.getProperty("DATASET_ID");
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      LOGGING_ENABLED_XML = loggingParameter;
      LOGS_BUCKET_NAME = bucketParameter;
      DATASET_ID = datasetParameter;

      if (fileData != null) {
        try {
          fileData.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}