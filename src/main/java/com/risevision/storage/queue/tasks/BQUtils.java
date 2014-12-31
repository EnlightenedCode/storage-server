package com.risevision.storage.queue.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.http.HttpRequestInitializer;
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
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.TableDataList;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.P12CredentialBuilder;
import com.risevision.storage.info.ServiceFailedException;


public class BQUtils {
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static HttpRequestInitializer credential;
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

  public static Object getSingleValueFromQuery(String query) throws ServiceFailedException {
    Bigquery bigquery = getBigquery();
    List<TableRow> tableRowList; 
    log.info(query);
    try {
      tableRowList = bigquery.jobs().query(
          Globals.PROJECT_ID,
          new QueryRequest().setQuery(query))
          .execute().getRows();
    } catch(IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(500);
    }

    if (tableRowList == null) { return null; }
    
    return tableRowList.get(0).getF().get(0).getV();
  }
  
  public static Bigquery getBigquery() {
    if (bigquery == null) {
      credential = new P12CredentialBuilder()
        .getCredentialFromP12File(Globals.RVMEDIA_P12_PATH, Globals.RVMEDIA_ID);
      bigquery = new Bigquery.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(Globals.STORAGE_APP_NAME).build();
    }
  
    return bigquery;
  }
}
