package com.risevision.storage;

public final class Globals {
	
	public static String BUCKET_NAME_PREFIX = "risemedialibrary-";
	
	public static final boolean IS_PRODUCTION = false;
	
	public static final String LOGS_BUCKET_NAME_TEST = "rise-storage-logs-test";
	public static final String LOGS_BUCKET_NAME_PRODUCTION = "rise-storage-logs";

	public static final String LOGS_BUCKET_NAME = IS_PRODUCTION ? LOGS_BUCKET_NAME_PRODUCTION : LOGS_BUCKET_NAME_TEST;
	
	public static final String DATASET_ID_TEST = "RiseStorageLogsTest";
	public static final String DATASET_ID_PRODUCTION = "RiseStorageLogs";
	
	public static final String DATASET_ID = IS_PRODUCTION ? DATASET_ID_PRODUCTION : DATASET_ID_TEST;
	
	public static final String PROJECT_ID = "452091732215";
	public static final String ACCESS_ID = "452091732215@developer.gserviceaccount.com";
	
	public static final String STORE_CLIENT_ID = "614513768474.apps.googleusercontent.com";
	
	public static String APP_PRODUCTION = "s~rvaserver2";
	public static String APP_TEST = "s~rvacore-test";
	public static String APP_TEST2 = "s~rvacore-test2";
	
	public static String DELETED_NAMESPACE = "-deleted-";
	
	public static int SEC_24HR = 24 * 60 * 60;
	public static int SEC_WEEK = 7 * 24 * 60 * 60;
	
	public static int MS_24HR = SEC_24HR * 1000;
	public static int MS_WEEK = SEC_WEEK * 1000;
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	public static final String ROOT = "root";
	
	public static final String SYSTEM_USER = "System";
	public static final String UNKNOWN_USER = "unknown";
	
	public static final String HQ_TIMEZONE = "America/Toronto";

        public static final String LOGGING_ENABLED_XML = "<Logging>"
                            + "<LogBucket>" + LOGS_BUCKET_NAME + "</LogBucket>"
                            + "<LogObjectPrefix>%bucketName%</LogObjectPrefix>"
                            + "</Logging>";
	
//	static public final String PREVIEW_SUFFIX = ".preview";
//	static public final String PREVIOUS_PARENT_SUFFIX = ".prev_parent"; 
	

}
