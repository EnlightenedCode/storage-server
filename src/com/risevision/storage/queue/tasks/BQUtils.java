package com.risevision.storage.queue.tasks;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.Insert;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.JobReference;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.risevision.storage.Globals;

public class BQUtils {
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private static final String BIGQUERY_SCOPE = "https://www.googleapis.com/auth/bigquery";
	
	private static final String WRITE_DISPOSITION_TRUNCATE = "WRITE_TRUNCATE";
	private static final String WRITE_DISPOSITION_APPEND = "WRITE_APPEND";
	
//	public static final String DATASET_ID = "LogsTesting";
	public static final String DATASET_ID = "RiseStorageLogsTest";
//	public static final String DATASET_ID = "RiseStorageLogs";
	
	protected static final Logger log = Logger.getAnonymousLogger();
	
	private static Bigquery bigquery;
	
	public static String startQuery(String query, String tableId) throws IOException {
		
		log.info("Starting Move Job: " + query);
		
		Bigquery bigquery = getBigquery();
		
		Job job = new Job();
	    JobConfiguration config = new JobConfiguration();
	    JobConfigurationQuery queryConfig = new JobConfigurationQuery();
	    config.setQuery(queryConfig);

	    job.setConfiguration(config);

	    queryConfig.setQuery(query);
	    
		// Describe the resulting table you are importing to:
		TableReference tableRef = new TableReference();
		tableRef.setDatasetId(DATASET_ID);
		tableRef.setTableId(tableId);
		tableRef.setProjectId(Globals.PROJECT_ID);
		queryConfig.setDestinationTable(tableRef);
		queryConfig.setWriteDisposition(WRITE_DISPOSITION_APPEND);

	    Insert insert = bigquery.jobs().insert(Globals.PROJECT_ID, job);
		JobReference jobRef =  insert.execute().getJobReference();

		// Waiting for job to complete.
//		pollResponse(bigquery, jobRef);
		
		return jobRef.getJobId();
	}

	public static String runFilesJob(String tableId, TableSchema schema, List<String> sources) throws IOException {
//		assert insertIds == null || rows.size() == insertIds.size();
		
		Bigquery bigquery = getBigquery();
		
		Job job = new Job();
		JobConfiguration config = new JobConfiguration();
		JobConfigurationLoad loadConfig = new JobConfigurationLoad();
		loadConfig.setSkipLeadingRows(1);
		config.setLoad(loadConfig);

		job.setConfiguration(config);
		
		// Set where you are importing from (i.e. the Google Cloud Storage paths).
		loadConfig.setSourceUris(sources);

		// Describe the resulting table you are importing to:
		TableReference tableRef = new TableReference();
		tableRef.setDatasetId(DATASET_ID);
		tableRef.setTableId(tableId);
		tableRef.setProjectId(Globals.PROJECT_ID);
		loadConfig.setDestinationTable(tableRef);
		loadConfig.setWriteDisposition(WRITE_DISPOSITION_TRUNCATE);

		loadConfig.setSchema(schema);

		Insert insert = bigquery.jobs().insert(Globals.PROJECT_ID, job);
		insert.setProjectId(Globals.PROJECT_ID);
		JobReference jobRef =  insert.execute().getJobReference();
		
		// Waiting for job to complete.
//		pollResponse(bigquery, jobRef);
		
		return jobRef.getJobId();
	}
	
	public static Job checkResponse(String jobId) throws IOException {
//		long startTime = System.currentTimeMillis();
//		long elapsedTime = 0;

		Bigquery bigquery = getBigquery();
		
//		while (true) {
			Job pollJob = bigquery.jobs().get(Globals.PROJECT_ID, jobId).execute();
//			elapsedTime = System.currentTimeMillis() - startTime;
			
			log.info("Status=" + pollJob.getStatus().getState() + 
//					"\nTime:" + elapsedTime + 
					"\nJobId=" + jobId);
			
			if (pollJob.getStatus().getState().equals("DONE")) {
				return pollJob;
				
			}
			// Pause execution for one second before polling job status again
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			return pollJob;
//		}
	}
	
	private static Bigquery getBigquery() {
		if (bigquery == null) {
			Environment env = ApiProxy.getCurrentEnvironment();
			String appId = env.getAppId();
			
			AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(BIGQUERY_SCOPE));
			bigquery = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(appId).build();
		}
		
		return bigquery;
	}
	
	
//	static public TableDataInsertAllResponse streamInsert(String tableId, List<Map<String, Object>> rows, List<String> insertIds) throws IOException {
//		
//		assert insertIds == null || rows.size() == insertIds.size();
//		
//		Environment env = ApiProxy.getCurrentEnvironment();
//		String appId = env.getAppId();
//		
//		AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(BIGQUERY_SCOPE));
//		Bigquery bigquery = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(appId).build();
//		
//		ArrayList<TableDataInsertAllRequest.Rows> rowList = new ArrayList<>();
//		
//		for (int i = 0; i < rows.size(); i++) {
//
//			Map<String, Object> row = rows.get(i);
//			
//			String insertId = null;
//
//			if (insertIds != null) {
//				insertId = insertIds.get(i);
//			}
//			
//			TableDataInsertAllRequest.Rows requestRow = new TableDataInsertAllRequest.Rows();
//			requestRow.setJson(row);
//			requestRow.setInsertId(insertId);
//			rowList.add(requestRow);
//		}
//
//		TableDataInsertAllRequest content = new TableDataInsertAllRequest().setRows(rowList);
//		
//		return bigquery.tabledata().insertAll(Globals.PROJECT_ID, DATASET_ID, tableId, content).execute();
//		
//	}
//	
//	static public Table insertTable(String tableId, TableSchema schema) throws IOException {
//		
//		Environment env = ApiProxy.getCurrentEnvironment();
//		String appId = env.getAppId();
//		
//		AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(BIGQUERY_SCOPE));
//		Bigquery bigquery = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(appId).build();
//		
//		Table table = new Table();
//		table.setSchema(schema);
//		TableReference tableRef = new TableReference();
//		tableRef.setDatasetId(DATASET_ID);
//		tableRef.setProjectId(Globals.PROJECT_ID);
//		tableRef.setTableId(tableId);
//		table.setTableReference(tableRef);
//
//		return bigquery.tables().insert(Globals.PROJECT_ID, DATASET_ID, table).execute();
//	}
//	
//	static public boolean checkResponse(TableDataInsertAllResponse response) {
//		
//		List<InsertErrors> insertErrors = response.getInsertErrors();
//		
//		boolean result = insertErrors == null;
//
//		if (!result) {
//
//			for (InsertErrors errors : insertErrors) {
//
//				List<ErrorProto> protos = errors.getErrors();
//
//				if (protos != null) {
//
//					for (ErrorProto error : protos)	{
//
//						Logger.getAnonymousLogger().warning("BQ Insert Error: " + error.getMessage());
//					}
//				}
//
//			}
//		}
//		
//		return result;
//	}
	
}
