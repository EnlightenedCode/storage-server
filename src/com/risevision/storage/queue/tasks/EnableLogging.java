package com.risevision.storage.queue.tasks;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.risevision.directory.documents.Company;
import com.risevision.storage.Globals;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.QueryParam;
import com.risevision.storage.amazonImpl.LoggingResponse;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.QueueName;
import com.risevision.storage.queue.QueueTask;

public class EnableLogging extends AbstractTask {
	
	private static final String LOGGING_ENABLED_XML = "<Logging>\n"
			+ "    <LogBucket>" + Globals.LOGS_BUCKET_NAME + "</LogBucket>\n"
			+ "    <LogObjectPrefix>%bucketName%</LogObjectPrefix>\n"
			+ "</Logging>";

	private static final String LOGGING_DISABLED_XML = "<Logging/>";

	private static int QUERY_LIMIT = 200;
	
	public static String runJob(String lastCompanyId) {
		
//		try {
			
			List<String> companyIds = getCompanyIDs(lastCompanyId);
			
			if (companyIds.isEmpty()) {
				return "Done";
			}
			
			for (String companyId: companyIds) {
				boolean enabled = false;
				try {
					enabled = verifyBucketLogging(companyId);

					log.info(String.format("Company %s has logging %s", companyId, enabled ? "enabled" : "disabled"));

				} catch (ServiceFailedException e) {
					if (e.getReason() == ServiceFailedException.NOT_FOUND) {
//						log.info("Bucket Not Found for Company: " + companyId);
					}
				}
				
				lastCompanyId = companyId;
			}
			
//			QueueFactory.getQueue(QueueName.STORAGE_LOG_ENABLE).add(withUrl("/queue")
//					.param(QueryParam.TASK, QueueTask.RUN_ENABLE_LOGGING_JOB)
//					.param(QueryParam.JOB_CURSOR, lastCompanyId)
//					.countdownMillis(1000 * 5)
//					.method(Method.GET));
			
			return "queued next job";
			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
//		}
		
	}
	
	private static List<String> getCompanyIDs(String lastId) {

		List<String> result = new ArrayList<String>();
		GetResponse<Document> response = null;
		
		String indexName = Company.INDEX_ALL;

		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build(); 
		Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

		GetRequest request = lastId != null ?  GetRequest.newBuilder().setReturningIdsOnly(true).setStartId(lastId).setIncludeStart(false).setLimit(QUERY_LIMIT).build() : 
			GetRequest.newBuilder().setReturningIdsOnly(true).setLimit(QUERY_LIMIT).build();
		response = index.getRange(request);

		for (Document d : response) {
			result.add(d.getId());
//			lastId = d.getId();
		}

		return result;

	} 
	
	public static boolean verifyBucketLogging(String companyId) throws ServiceFailedException {
		boolean enabled = false;

		String bucketName = MediaLibraryService.getBucketName(companyId);

		enabled = checkBucketLogging(bucketName);

		if (!enabled) {
			updateBucketLogging(bucketName, true);
			
			return false;
		}
		
		return true;
	}

	private static boolean checkBucketLogging(String bucketName) throws ServiceFailedException {
		MediaLibraryService service = MediaLibraryService.getInstance();

		try {
			InputStream stream = service.getBucketProperty(bucketName, "logging");
			if (stream != null) {
				LoggingResponse loggingResponse = new LoggingResponse(stream);
				
				return loggingResponse.getLogging();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static void updateBucketLogging(String bucketName, boolean enabled) throws ServiceFailedException {
		MediaLibraryService service = MediaLibraryService.getInstance();

		try {
			String propertyXMLdoc = enabled ? LOGGING_ENABLED_XML.replace("%bucketName%", bucketName) : LOGGING_DISABLED_XML;
			
			service.updateBucketProperty(bucketName, "logging", propertyXMLdoc);
		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

