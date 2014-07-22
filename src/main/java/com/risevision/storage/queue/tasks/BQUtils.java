package com.risevision.storage.queue.tasks;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.Insert;
import com.google.api.services.bigquery.model.*;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.risevision.storage.Globals;

public class BQUtils {
	
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
	private static final String BIGQUERY_SCOPE = "https://www.googleapis.com/auth/bigquery";
	
	public static final String WRITE_DISPOSITION_TRUNCATE = "WRITE_TRUNCATE";
	public static final String WRITE_DISPOSITION_APPEND = "WRITE_APPEND";
	
	protected static final Logger log = Logger.getAnonymousLogger();
	
	private static Bigquery bigquery;
	
	public static String startQuery(String query, String tableId, String tableDisposition) throws IOException {
		
		log.info("Starting Query: " + query);
		
		Bigquery bigquery = getBigquery();
		
		Job job = new Job();
	    JobConfiguration config = new JobConfiguration();
	    JobConfigurationQuery queryConfig = new JobConfigurationQuery();
	    config.setQuery(queryConfig);

	    job.setConfiguration(config);

	    queryConfig.setQuery(query);
	    
		// Describe the resulting table you are importing to:
		TableReference tableRef = new TableReference();
		tableRef.setDatasetId(Globals.DATASET_ID);
		tableRef.setTableId(tableId);
		tableRef.setProjectId(Globals.PROJECT_ID);
		queryConfig.setDestinationTable(tableRef);
		queryConfig.setWriteDisposition(tableDisposition);

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
		tableRef.setDatasetId(Globals.DATASET_ID);
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
	
	public static Job getJob(String jobId) throws IOException {
//		long startTime = System.currentTimeMillis();
//		long elapsedTime = 0;

		Bigquery bigquery = getBigquery();
		
//		while (true) {
			Job pollJob = bigquery.jobs().get(Globals.PROJECT_ID, jobId).execute();
//			elapsedTime = System.currentTimeMillis() - startTime;
			
			log.info("Status=" + pollJob.getStatus().getState() + 
//					"\nTime:" + elapsedTime + 
					"\nJobId=" + jobId);
			
			return pollJob;
//		}
	}

        public static List<TableRow> getTableRows(String tableId) {
          int initialCapacity = 2000;
          List<TableRow> rowList = new ArrayList<TableRow>(initialCapacity);
          if (tableId == null) {
            return rowList;
          }

          Bigquery bigquery = getBigquery();
          try {
            Bigquery.Tabledata.List listRequest = bigquery.tabledata().list(
                                        Globals.PROJECT_ID
                                       ,Globals.DATASET_ID
                                       ,tableId);

            TableDataList dataListResult;
            do {
              dataListResult = listRequest.execute();
              rowList.addAll(dataListResult.getRows());
              listRequest.setPageToken(dataListResult.getPageToken());
            } while (dataListResult.getPageToken() != null);
          } catch (IOException e) {
            log.warning(e.getMessage());
            return new ArrayList<TableRow>();
          }

          return rowList;
        }

        public static Object getSingleValueFromQuery(String query) {
          Bigquery bigquery = getBigquery();
          TableRow tableRow; 
          log.info(query);
          try {
            tableRow = bigquery.jobs().query(Globals.PROJECT_ID,
                                  new QueryRequest().setQuery(query))
                               .execute().getRows().get(0);
          } catch(IOException e) {
            log.warning(e.getMessage());
            return null;
          } catch(NullPointerException e) {
            log.warning(e.getMessage());
            return null;
          }

          List<TableCell> cells = tableRow.getF();
          return cells.get(0).getV();
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
  }
