package com.risevision.storage.jobs;

import java.io.IOException;
import java.util.ArrayList;
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
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.JobReference;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse.InsertErrors;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.risevision.storage.Globals;
import com.risevision.storage.MediaLibraryLogReader;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.MediaLibraryServiceImpl;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;

public class BigQueryImportJob {
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private static final String BIGQUERY_SCOPE = "https://www.googleapis.com/auth/bigquery";
	
	private static final String LOGS_BUCKET_NAME = "rva-logs-bucket";

	
	public static void runStorageJob() throws IOException {
		TableSchema schema = getStorageSchema();

		List<String> sources = new ArrayList<String>();
		sources.add("gs://rva-logs-bucket/risemedialibrary-fb788f1f-7730-44fd-8e00-20066409f51f_storage_2013_09_30_07_00_00_0d467_v0");

		
		runJob("StorageLogs", schema, sources);
	}
	
	public static int runUsageJob() throws IOException, ServiceFailedException {
		TableSchema schema = getUsageSchema();
		List<String> sources = new ArrayList<String>();

		MediaLibraryService service = new MediaLibraryServiceImpl();

		for (MediaItemInfo item: service.getBucketItems(LOGS_BUCKET_NAME)) {
			if (item.getKey().contains("usage")) {
				sources.add("gs://rva-logs-bucket/" + item.getKey());
				
				if (sources.size() > 10) {
					break;
				}
			}
		}

		runJob("UsageLogs", schema, sources);
		
		return sources.size();
	}
	
	private static void runJob(String tableId, TableSchema schema, List<String> sources) throws IOException {
//		assert insertIds == null || rows.size() == insertIds.size();
		
		Environment env = ApiProxy.getCurrentEnvironment();
		String appId = env.getAppId();
		
		AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(BIGQUERY_SCOPE));
		Bigquery bigquery = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(appId).build();
		
		Job job = new Job();
		JobConfiguration config = new JobConfiguration();
		JobConfigurationLoad loadConfig = new JobConfigurationLoad();
		loadConfig.setSkipLeadingRows(1);
		config.setLoad(loadConfig);

		job.setConfiguration(config);
		
//		MediaLibraryLogReader.re
		
		
		// Set where you are importing from (i.e. the Google Cloud Storage paths).
		loadConfig.setSourceUris(sources);

		// Describe the resulting table you are importing to:
		TableReference tableRef = new TableReference();
		tableRef.setDatasetId("LogsTesting");
		tableRef.setTableId(tableId);
		tableRef.setProjectId(Globals.PROJECT_ID);
		loadConfig.setDestinationTable(tableRef);

		loadConfig.setSchema(schema);

		// Also set custom delimiter or header rows to skip here....
		// [not shown].

		Insert insert = bigquery.jobs().insert(Globals.PROJECT_ID, job);
		insert.setProjectId(Globals.PROJECT_ID);
		JobReference jobRef =  insert.execute().getJobReference();
		

		// Waiting for job to complete.
		pollResponse(bigquery, jobRef);
	}
	
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
	
	private static Job pollResponse(Bigquery bigquery, JobReference jobRef)	throws IOException {
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;

		while (true) {
			Job pollJob = bigquery.jobs().get(Globals.PROJECT_ID, jobRef.getJobId()).execute();
			elapsedTime = System.currentTimeMillis() - startTime;
			Logger.getAnonymousLogger().warning("Status=" + pollJob.getStatus().getState() + "\nTime:" + elapsedTime + "\nJobId=" + jobRef.getJobId());
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
		}
	}
	
	static public boolean checkResponses(TableDataInsertAllResponse response) {
		
		List<InsertErrors> insertErrors = response.getInsertErrors();
		
		boolean result = insertErrors == null;

		if (!result) {

			for (InsertErrors errors : insertErrors) {

				List<ErrorProto> protos = errors.getErrors();

				if (protos != null) {

					for (ErrorProto error : protos)	{

						Logger.getAnonymousLogger().warning("BQ Insert Error: " + error.getMessage());
					}
				}

			}
		}
		
		return result;
	}
	
}

