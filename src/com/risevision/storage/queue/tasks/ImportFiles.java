package com.risevision.storage.queue.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.storage.Globals;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.MediaLibraryServiceImpl;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.queue.QueueServlet;

public class ImportFiles extends AbstractTask {
	
	private static final String LOGS_BUCKET_URL = "gs://" + Globals.LOGS_BUCKET_NAME + "/";
	
	private static final int JOB_USAGE = 1;
	private static final int JOB_STORAGE = 0;
	
	private static final String USAGE_TABLE = "UsageLogs";
	private static final String USAGE_TABLE_TEMP = "UsageLogsTemp";
	private static final String STORAGE_TABLE = "StorageLogs";
	private static final String STORAGE_TABLE_TEMP = "StorageLogsTemp";
	
//	private static long MAX_BYTES_PER_POST = 1 * 1000 * 1000; // not exactly a megabyte, leave some buffer 

	public static void runJob() throws Exception {
		runJob(JOB_STORAGE);
		
		QueueServlet.enqueueJob(Integer.toString(JOB_USAGE));
	}
	
	public static String runJob(int jobType) throws Exception {
		
//		try {
			
			List<String> sources = new ArrayList<String>();
			List<String> files = new ArrayList<String>();

			MediaLibraryService service = MediaLibraryService.getInstance();
			
			String lastItem = null;
			String logDate = "";
			while (sources.size() < 200) {
				List<MediaItemInfo> items = service.getBucketItems(Globals.LOGS_BUCKET_NAME, null, lastItem);

				for (MediaItemInfo item: items) {
					
					// process Storage logs first
					if (item.getKey().contains("storage") && jobType == JOB_STORAGE) {
						if (RiseUtils.strIsNullOrEmpty(logDate)) {
							logDate = getStorageLogDate(item.getKey());
						}
						
						if (item.getKey().contains(logDate)) {
							sources.add(LOGS_BUCKET_URL + item.getKey());
							files.add(item.getKey());
						}
					}
					else if (item.getKey().contains("usage") && jobType == JOB_USAGE) {
						sources.add(LOGS_BUCKET_URL + item.getKey());
						files.add(item.getKey());
					}
					
					if (sources.size() >= 200) {
						break;
					}
				}
				
				if (items.size() < MediaLibraryServiceImpl.MAX_KEYS) {
					break;
				}
				
				lastItem = items.get(items.size() - 1).getKey();
			}
			
			if (sources.size() == 0) {
				log.info("Done");
				
				return "Done";
			}
			
			String jobId;
			if (jobType == JOB_STORAGE) {
				jobId = runStorageJob(sources);
			}
			else {
				jobId =  runUsageJob(sources);
			}
			
			String filesString = RiseUtils.listToString(files, ",");
			
			QueueServlet.enqueueCheckImportJob(jobId, Integer.toString(jobType), filesString);
			
			log.info("Files (" + files.size() + ") first: " + files.get(0) + " last: " + files.get(files.size() - 1));
			return "Queued";
			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
//		}
		
	}
	
	public static void postProcess(String filesString, int jobType) throws Exception {
		// delete the files
		List<String> files = new ArrayList<String>(Arrays.asList(filesString.split(",")));
		
		if (files.size() > 0) {
			MediaLibraryService service = MediaLibraryService.getInstance();
			
			log.info("Removing Files (" + files.size() + ") first: " + files.get(0) + " last: " + files.get(files.size() - 1));
			
			List<String> failedFiles = service.deleteMediaItems(Globals.LOGS_BUCKET_NAME, files);
			
			if (failedFiles.size() > 0) {
				filesString = RiseUtils.listToString(failedFiles, ",");
				
				QueueServlet.enqueueCheckImportJob("", Integer.toString(jobType), filesString);
				
				log.info("Requeued delete job for: " + filesString);
				
				return;
			}
		}
		
		if (jobType == ImportFiles.JOB_STORAGE) {
			runStorageMoveJob(files.get(0));
		}
		else {
			runUsageMoveJob();
		}
		
	}
	
	public static void runStorageMoveJob(String filename) throws Exception {
		String dateToken = getStorageLogDate(filename);
		DateFormat inputDateFormat = new SimpleDateFormat("yyyy_MM_dd");

		Date date = inputDateFormat.parse(dateToken);
		
		String query =
				"SELECT bucket, storage_byte_hours, SEC_TO_TIMESTAMP(" + date.getTime() / 1000 + ") as date "
						+ "FROM [" + Globals.DATASET_ID + "." + STORAGE_TABLE_TEMP + "];";
				
//		try {
			
		    String jobId = BQUtils.startQuery(query, STORAGE_TABLE);
//		    BQUtils.checkResponse(jobId);
			
		    QueueServlet.enqueueCheckMoveJob(jobId, Integer.toString(JOB_STORAGE));
			
//			return sources.size() + " of " + items.size();
			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
//		}
		
	}
	
	public static void runUsageMoveJob() throws Exception {
		
		String query =
				"SELECT USEC_TO_TIMESTAMP(INTEGER(time_micros)) as time_timestamp, * "
						+ "FROM [" + Globals.DATASET_ID + "." + USAGE_TABLE_TEMP + "];";
				
//		try {
			
		    String jobId = BQUtils.startQuery(query, USAGE_TABLE);
//		    BQUtils.checkResponse(jobId);
			
		    QueueServlet.enqueueCheckMoveJob(jobId, Integer.toString(JOB_USAGE));
			
//			return sources.size() + " of " + items.size();
			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
//		}
		
	}
	
	private static String getStorageLogDate(String filename) {
		// Parse Date part of the Storage log filename: [bucket]_storage_2014_02_09_08_00_00_0554a_v0
		String logDate = filename.substring(filename.indexOf("_storage_") + "_storage_".length());
		logDate = logDate.substring(0, 10);
		
		return logDate;
	}
	
//	private static String getStorageLogDate(String filename) {
//		String logDate = "";
//		// Parse Date part of the Storage log filename: [bucket]_storage_2014_02_09_08_00_00_0554a_v0
//		String[] tokens = filename.split("_");
//		int tokenCount = -1;
//		for (String token: tokens) {
//			if (tokenCount == -1 && token.equals("storage")) {
//				tokenCount = 0;
//			}
//			
//			if (tokenCount >= 0) {
//				logDate += token;
//			}
//			
//			if (tokenCount == 3) {
//				break;
//			}
//		}
//		
//		return logDate;
//	}
	
	private static String runStorageJob(List<String> sources) throws Exception {
		TableSchema schema = getStorageSchema();
		
		
//		recordJobFiles("StorageLogFiles", sources);
		
		String jobId =  BQUtils.runFilesJob(STORAGE_TABLE_TEMP, schema, sources);

//		FileTransferJob transferJob = new FileTransferJob(jobId, JOB_STORAGE, sources);
//		transferJob.put();
		
		return jobId;
		
	}
	
	private static String runUsageJob(List<String> sources) throws Exception {
		TableSchema schema = getUsageSchema();
		
//		recordJobFiles("UsageLogFiles", sources);

		String jobId = BQUtils.runFilesJob(USAGE_TABLE_TEMP, schema, sources);
		
//		FileTransferJob transferJob = new FileTransferJob(jobId, JOB_USAGE, sources);
//		transferJob.put();
		
		return jobId;
		
	}
	
	
//	private static void recordJobFiles(String tableId, List<String> sources) throws Exception {
//		
//		List<Map<String, Object>> requestRows = new ArrayList<>();
//		List<String> requestInsertIds = new ArrayList<>(); 
//		
//		long requestExportBytes = 2;
//		
//		BQUtils.insertTable(tableId, getFilenameSchema());
//		
//		for (String source: sources) {
//						
//			Map<String, Object> requestRow = new HashMap<>();
//				
//			requestRow.put("fileName", source);
////			requestRow.put("status", 0);
//
//			long requestRowBytes = requestRow.toString().getBytes("UTF-8").length + 1; // Assumes a comma for every array item 
//				
//			// Should never happen
//			if (requestExportBytes + requestRowBytes > MAX_BYTES_PER_POST) {
//			
//				log.info("Requests: Inserting " + Long.toString(requestExportBytes) + " bytes");
//			
//				if (BQUtils.checkResponse(BQUtils.streamInsert(tableId, requestRows, requestInsertIds))) {
//
//					log.info(String.format("Inserted %d request rows", requestRows.size()));
//				}
//
//				requestRows = new ArrayList<>();
//				requestInsertIds = new ArrayList<>(); 
//				requestExportBytes = 2;
//			}
//				
//			requestExportBytes += requestRowBytes; 
//								
//			requestRows.add(requestRow);
//			requestInsertIds.add(source);
//
//		}
//		
//		if (requestRows.size() > 0) {
//
//			try {
//
//				if (requestRows.size() > 0) {
//					
//					log.info("Requests: Inserting " + Long.toString(requestExportBytes) + " bytes");
//					if (BQUtils.checkResponse(BQUtils.streamInsert(tableId, requestRows, requestInsertIds))) {
//
//						log.info(String.format("Inserted %d request rows", requestRows.size()));
//					}
//				}
//
//			} catch (Exception ex) {
//				log.severe("Error inserting into BQ: " + ex.getMessage());
//				throw ex;
//			}
//			
//		} else {
//			
//			log.info("No display-related data to insert this time, skipping.");
//			
//		}
//		
//	}
	
//	private static TableSchema getFilenameSchema() {
//		TableSchema schema = new TableSchema();
//		List<TableFieldSchema> tableFieldSchema = new ArrayList<TableFieldSchema>();
//		TableFieldSchema schemaEntry = new TableFieldSchema();
//		schemaEntry.setName("fileName");
//		schemaEntry.setType("string");
//		tableFieldSchema.add(schemaEntry);
//		schema.setFields(tableFieldSchema);
//		
//		return schema;
//	}
	
	private static TableSchema getStorageSchema() {
		TableSchema schema = new TableSchema();

		List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();
		
		fields.add(getTableField("bucket", "string"));
		fields.add(getTableField("storage_byte_hours", "string"));
		
		schema.setFields(fields);
		
		return schema;
	}
	
	private static TableSchema getUsageSchema() {
		TableSchema schema = new TableSchema();

		List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();

		fields.add(getTableField("time_micros", "string"));
		fields.add(getTableField("c_ip", "string"));
		fields.add(getTableField("c_ip_type", "integer"));
		fields.add(getTableField("c_ip_region", "string"));
		fields.add(getTableField("cs_method", "string"));
		fields.add(getTableField("cs_uri", "string"));
		fields.add(getTableField("sc_status", "integer"));
		fields.add(getTableField("cs_bytes", "integer"));
		fields.add(getTableField("sc_bytes", "integer"));
		fields.add(getTableField("time_taken_micros", "integer"));
		fields.add(getTableField("cs_host", "string"));
		fields.add(getTableField("cs_referer", "string"));
		fields.add(getTableField("cs_user_agent", "string"));
		fields.add(getTableField("s_request_id", "string"));
		fields.add(getTableField("cs_operation", "string"));
		fields.add(getTableField("cs_bucket", "string"));
		fields.add(getTableField("cs_object", "string"));
		
		schema.setFields(fields);
		
		return schema;
	}
		
	private static TableFieldSchema getTableField(String name, String type) {
		TableFieldSchema tableField = new TableFieldSchema();
		tableField.setName(name);
		tableField.setType(type);
		
		return tableField;
	}
	

	
}

