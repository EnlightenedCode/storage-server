package com.risevision.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.risevision.common.client.utils.RiseUtils;
import com.risevision.storage.amazonImpl.LoggingResponse;
import com.risevision.storage.cache.CompanyFileLogs;
import com.risevision.storage.cache.CompanyUsage;
import com.risevision.storage.csv.CSVReader;
import com.risevision.storage.entities.FileLog;
import com.risevision.storage.entities.Usage;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.MediaItemStorageInfo;
import com.risevision.storage.info.MediaItemUsageInfo;
import com.risevision.storage.info.ServiceFailedException;

public class MediaLibraryLogReader {

	private static final String LOGS_BUCKET_NAME = "rva-logs-bucket";
	private static final String LOGGING_ENABLED_XML = "<Logging>\n"
			+ "    <LogBucket>" + LOGS_BUCKET_NAME + "</LogBucket>\n"
			+ "    <LogObjectPrefix>%bucketName%</LogObjectPrefix>\n"
			+ "</Logging>";

	private static final String LOGGING_DISABLED_XML = "<Logging/>";

	String[] headerStrings = { "time_micros", "c_ip", "c_ip_type",
			"c_ip_region", "cs_method", "cs_uri", "sc_status", "cs_bytes",
			"sc_bytes", "time_taken_micros", "cs_host", "cs_referer",
			"cs_user_agent", "s_request_id", "cs_operation", "cs_bucket",
			"cs_object" };

	String[] sampleResponse = {
			"1380216483252000",
			"8.35.201.116",
			"1",
			"",
			"GET",
			"/first-bucket-test",
			"200",
			"0",
			"1727",
			"148000",
			"commondatastorage.googleapis.com",
			"",
			"Google-HTTP-Java-Client/1.8.3-beta (gzip) AppEngine-Google; (+http://code.google.com/appengine; appid: s~rva-test),gzip(gfe)",
			"AEnB2UpWVs02TxrCtO9jqjnFeBGbAeqJ70R7FCVO-ML6W81Tfheq6gq68f4c1XbfWLtZDCxhjFO9CwF5JwwEsjzLSiAV_Txhtw",
			"GET_Bucket", "first-bucket-test", "" };
	
	
	public static String parseBucketLogs(String companyId) {
		return parseBucketStorageLogs(companyId) + "\n"
				+ "Usage Logs: \n" + parseBucketUsageLogs(companyId)
				;
	}
	
	private static String parseBucketUsageLogs(String companyId) {
		List<MediaItemInfo> mediaItems = retrieveBucketLogList("risemedialibrary-" + companyId + "_usage");
		String response = "";
		long dataCounter = 0;
		
		if (mediaItems != null) {
			CompanyUsage companyUsage = getCompanyUsage(companyId);
			Usage usage = companyUsage.usage;
			
			CompanyFileLogs fileLogs = getCompanyFileLogs(companyId);
			
			for (MediaItemInfo mediaItem : mediaItems.subList(0, 10)) {
				List<MediaItemUsageInfo> usageItems = retrieveUsageLog(mediaItem.getKey());
				
				for (MediaItemUsageInfo usageItem: usageItems) {
					if (!RiseUtils.strIsNullOrEmpty(usageItem.getObject()) && usageItem.getStatus() != 404) {
						Map<String, FileLog> perIpLogs = fileLogs.fileLogs.get(usageItem.getObject());
						
						if (perIpLogs == null) {
							perIpLogs = new HashMap<String, FileLog>();
							fileLogs.fileLogs.put(usageItem.getObject(), perIpLogs);
						}
						
						FileLog fileLog = perIpLogs.get(usageItem.getIp());
						if (fileLog == null) {
							fileLog = new FileLog(companyId, usageItem.getObject(), usageItem.getIp());
							perIpLogs.put(usageItem.getIp(), fileLog);
						}
						
						fileLog.totalRequests++;

						if (usageItem.getStatus() == 200) {
							fileLog.totalBandwidth += usageItem.getOutBytes();
							dataCounter += usageItem.getOutBytes();
						}
						else {
							fileLog.cachedRequests++;
						}

						fileLog.setChanged();

//						response += "File request - Name:" + usageItem.getObject();
//						response += " Ip:" + usageItem.getIp() + " Status:" + usageItem.getStatus();
//						
//						if (usageItem.getUserAgent().contains("Java/1.")) {
//							response += " From:Rise Cache";
//						}
//						
//						if (usageItem.getStatus() == 200) {
//							response += " Out Bytes:" + usageItem.getOutBytes() + "\n";
////							response += " URI:" + item.getUri() + "\n";
//
//							dataCounter += usageItem.getOutBytes();
//						}
//						else {
//							response += " Cached\n";
//						}
					}
//					else {
//						response += "Bucket request - URI:" + usageItem.getUri();
//						response += " Method:" + usageItem.getMethod() + " Status:" + usageItem.getStatus() + "\n";
//					}
					
				}
			}
			
			usage.totalBandwidth += dataCounter;
			usage.put();

			CacheUtils.saveToCache(companyUsage);
			
			for (Map<String, FileLog> perIpLogs: fileLogs.fileLogs.values()) {
				for (FileLog fileLog: perIpLogs.values()) {
					if (fileLog.isChanged()) {
						fileLog.put();
					}
				}
			}
			
			CacheUtils.saveToCache(fileLogs);
		}
		
		response += "Total Out Bytes:" + dataCounter;
		
		return response;
	}
	
	private static String parseBucketStorageLogs(String companyId) {
		List<MediaItemInfo> items = retrieveBucketLogList("risemedialibrary-" + companyId + "_storage");
		
		CompanyUsage companyUsage = getCompanyUsage(companyId);
		Usage usage = companyUsage.usage;

		String response = "";
		MediaItemStorageInfo storageItem = null;

		if (items != null) {
			for (MediaItemInfo item : items) {
				storageItem = retrieveStorageLog(item.getKey());
				
				if (usage.averageStorage == 0) {
					usage.averageStorage = storageItem.getBytes();
				} else {
					usage.averageStorage = (usage.averageStorage * 29 + storageItem.getBytes()) / 30;
				}
			}
		}
		
		if (storageItem != null) {
			response += "Log Name:" + storageItem.getLogName() + "\n";
			response += "Out Bytes:" + storageItem.getBytes() + "\n";
			
			usage.latestStorage = storageItem.getBytes();
		}
		
		response += "Average Bytes:" + usage.averageStorage;
		
		usage.put();

		CacheUtils.saveToCache(companyUsage);
		
		return response;
	}
	
	public static List<MediaItemInfo> retrieveBucketLogList(String bucketName) {
		try {
			MediaLibraryService service = MediaLibraryService.getInstance();
		

			return service.getBucketItems(LOGS_BUCKET_NAME, bucketName, null);
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static List<MediaItemUsageInfo> retrieveUsageLog(String logName) {
		try {
			MediaLibraryService service = MediaLibraryService.getInstance();
			
//			InputStream stream = service.getMediaItem("rva-logs-bucket", "first-bucket-test_usage_2013_09_25_07_00_00_09863_v0");
			InputStream stream = service.getMediaItem(LOGS_BUCKET_NAME, logName);
			
			CSVReader reader = new CSVReader(new InputStreamReader(stream));
			
			List<String[]> s = reader.readAll();
			s.remove(0);
			
			List<MediaItemUsageInfo> usageItems = new ArrayList<MediaItemUsageInfo>();
			
			for (String[] line: s) {
				MediaItemUsageInfo item = new MediaItemUsageInfo();
				
//				"time_micros", "c_ip", "c_ip_type",
//				"c_ip_region", "cs_method", "cs_uri", "sc_status", "cs_bytes",
//				"sc_bytes", "time_taken_micros", "cs_host", "cs_referer",
//				"cs_user_agent", "s_request_id", "cs_operation", "cs_bucket",
//				"cs_object"
				
				item.setTime(new Date(RiseUtils.strToInt(line[0], 0)));
				item.setIp(line[1]);
				item.setMethod(line[4]);
				item.setUri(line[5]);
				item.setStatus(RiseUtils.strToInt(line[6], 0));
				item.setInBytes(RiseUtils.strToLong(line[7], 0));
				item.setOutBytes(RiseUtils.strToLong(line[8], 0));
				item.setUserAgent(line[12]);	
				item.setObject(line[16]);
				
//				int index = 0;
//				for (String value: line) {
//					if (index != 2 && index != 3 && index != 9 && index != 12) {
//						response += value + " \t| ";
//					}
//					index++;
//				}
//				response += "\n";
				
				usageItems.add(item);
			}
			
			reader.close();
			
			return usageItems;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static MediaItemStorageInfo retrieveStorageLog(String logName) {
		try {
			MediaLibraryService service = MediaLibraryService.getInstance();
			
//			InputStream stream = service.getMediaItem("rva-logs-bucket", "first-bucket-test_usage_2013_09_25_07_00_00_09863_v0");
			InputStream stream = service.getMediaItem(LOGS_BUCKET_NAME, logName);
			
			CSVReader reader = new CSVReader(new InputStreamReader(stream));

			List<String[]> s = reader.readAll();
			
			MediaItemStorageInfo storageItem = new MediaItemStorageInfo();
			
//			storageItem.setTime(new Date(RiseUtils.strToInt(line[0], 0)));
//			storageItem.setLogName(s.get(1)[0]);
			storageItem.setLogName(logName);
			storageItem.setBytes(RiseUtils.strToLong(s.get(1)[1], 0));
				
			reader.close();
			
			return storageItem;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean verifyBucketLogging(String bucketName) {
		if (!checkBucketLogging(bucketName)) {
			updateBucketLogging(bucketName, true);
			
			return false;
		}
		return true;
	}

	private static boolean checkBucketLogging(String bucketName) {
		MediaLibraryService service = MediaLibraryService.getInstance();

		try {
			InputStream stream = service.getBucketProperty(bucketName, "logging");
			if (stream != null) {
				LoggingResponse loggingResponse = new LoggingResponse(stream);
				
				return loggingResponse.getLogging();
			}
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static void updateBucketLogging(String bucketName, boolean enabled) {
		MediaLibraryService service = MediaLibraryService.getInstance();

		try {
			String propertyXMLdoc = enabled ? LOGGING_ENABLED_XML.replace("%bucketName%", bucketName) : LOGGING_DISABLED_XML;
			
			service.updateBucketProperty(bucketName, "logging", propertyXMLdoc);
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static CompanyUsage getCompanyUsage(String companyId) {
		CompanyUsage companyUsage = CacheUtils.get(CompanyUsage.class, companyId);
			
		if (companyUsage == null || companyUsage.usage == null) {
			Usage usage = new Usage(companyId);
			
			if (companyUsage == null) {
				companyUsage = new CompanyUsage(companyId, usage);
			}
			else {
				companyUsage.usage = usage;
			}
		}

		return companyUsage;
	}
	
	private static CompanyFileLogs getCompanyFileLogs(String companyId) {
		CompanyFileLogs fileLogs = CacheUtils.get(CompanyFileLogs.class, companyId);
		
		if (fileLogs == null || fileLogs.fileLogs == null) {
			Map<String, Map<String, FileLog>> fileLogsMap = new HashMap<>();
			
			if (fileLogs == null) {
				fileLogs = new CompanyFileLogs(companyId, fileLogsMap);
			}
			else {
				fileLogs.fileLogs = fileLogsMap;
			}
		}
		
		return fileLogs;
	}
	
}
