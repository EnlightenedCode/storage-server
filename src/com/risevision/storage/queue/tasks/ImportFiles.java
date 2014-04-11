package com.risevision.storage.queue.tasks;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.MediaLibraryServiceImpl;
import com.risevision.storage.QueryParam;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.queue.QueueTask;

public class ImportFiles extends AbstractTask {
	
	private static final String LOGS_BUCKET_NAME = "rva-logs-bucket";
	private static final String LOGS_BUCKET_URL = "gs://" + LOGS_BUCKET_NAME + "/";
	
	private static final int JOB_USAGE = 1;
	private static final int JOB_STORAGE = 0;
	
	private static final String USAGE_TABLE = "UsageLogs";
	private static final String STORAGE_TABLE = "StorageLogs";
	private static final String STORAGE_TABLE_TEMP = "StorageLogsTemp";
	
//	private static long MAX_BYTES_PER_POST = 1 * 1000 * 1000; // not exactly a megabyte, leave some buffer 


	public static String runJob() throws Exception {
		
//		try {
			
			List<String> sources = new ArrayList<String>();

			MediaLibraryService service = new MediaLibraryServiceImpl();
			
			List<MediaItemInfo> items = service.getBucketItems(LOGS_BUCKET_NAME);
			int jobType = -1;
			String logDate = "";
			for (MediaItemInfo item: items) {
				
				// process Storage logs first
				if (item.getKey().contains("storage") && jobType != JOB_USAGE) {
					if (RiseUtils.strIsNullOrEmpty(logDate)) {
						logDate = getStorageLogDate(item.getKey());
					}
					
					if (item.getKey().contains(logDate)) {
						sources.add(LOGS_BUCKET_URL + item.getKey());
						jobType = JOB_STORAGE;			
					}
				}
				else if (item.getKey().contains("usage") && jobType != JOB_STORAGE) {
					sources.add(LOGS_BUCKET_URL + item.getKey());
					jobType = JOB_USAGE;
				} 
				
				if (sources.size() > 10) {
					break;
				}
			}
			
			String jobId;
			if (jobType == JOB_STORAGE) {
				jobId = runStorageJob(sources);
			}
			else {
				jobId =  runUsageJob(sources);
			}
			
			String filesString = RiseUtils.listToString(sources, ",");
			
			QueueFactory.getDefaultQueue().add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.CHECK_IMPORT_JOB)
					.param(QueryParam.JOB_ID, jobId)
					.param(QueryParam.JOB_TYPE, Integer.toString(jobType))
					.param(QueryParam.JOB_FILES, filesString)
					.countdownMillis(1000 * 10)
					.method(Method.POST));
			
			return sources.size() + " of " + items.size();
			
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
		
		MediaLibraryService service = new MediaLibraryServiceImpl();
		
//		service.deleteMediaItems(LOGS_BUCKET_NAME, files);
		
		if (jobType == ImportFiles.JOB_STORAGE) {
			runMoveJob(files.get(0));
		}
		
		log.info("Job Done - File List:" + filesString);
	}
	
	public static void runMoveJob(String filename) throws Exception {
		String dateToken = getStorageLogDate(filename);
		
		String query =
				"SELECT bucket, storage_byte_hours, '" + dateToken + "' as date "
						+ "FROM [" + BQUtils.DATASET_ID + "." + STORAGE_TABLE_TEMP + "];";
				
		
//		try {
			
		    String jobId = BQUtils.startQuery(query, STORAGE_TABLE);
//		    BQUtils.checkResponse(jobId);
			
			QueueFactory.getDefaultQueue().add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.CHECK_MOVE_JOB)
					.param(QueryParam.JOB_ID, jobId)
//					.param(QueryParam.JOB_TYPE, Integer.toString(JOB_STORAGE))
//					.param(QueryParam.JOB_FILES, filesString)
					.countdownMillis(1000 * 10)
					.method(Method.POST));
			
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

		String jobId = BQUtils.runFilesJob(USAGE_TABLE, schema, sources);
		
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
		fields.add(getTableField("c_ip_type", "string"));
		fields.add(getTableField("c_ip_region", "string"));
		fields.add(getTableField("cs_method", "string"));
		fields.add(getTableField("cs_uri", "string"));
		fields.add(getTableField("sc_status", "string"));
		fields.add(getTableField("cs_bytes", "string"));
		fields.add(getTableField("sc_bytes", "string"));
		fields.add(getTableField("time_taken_micros", "string"));
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

