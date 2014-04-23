package com.risevision.storage.queue.tasks;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
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
import com.risevision.storage.gcs.StorageHelper;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.QueueName;
import com.risevision.storage.queue.QueueTask;

public class EnableLogging extends AbstractTask {
	
//	private static final String LOGGING_ENABLED_XML = "<Logging>\n"
//			+ "    <LogBucket>" + Globals.LOGS_BUCKET_NAME + "</LogBucket>\n"
//			+ "    <LogObjectPrefix>%bucketName%</LogObjectPrefix>\n"
//			+ "</Logging>";
//
//	private static final String LOGGING_DISABLED_XML = "<Logging/>";

	private static int QUERY_LIMIT = 200;
	
	public static String runJob(String userId, String lastCompanyId) {
		
//		try {
			
			List<String> companyIds = getCompanyIDs(lastCompanyId);
			
			if (companyIds.isEmpty()) {
				return "Done";
			}
			
			for (String companyId: companyIds) {
				boolean enabled = false;
				try {
					enabled = verifyBucketLogging(companyId, userId);

					log.info(String.format("Company %s has logging %s", companyId, enabled ? "enabled" : "disabled"));

				} catch (ServiceFailedException e) {
					if (e.getReason() == ServiceFailedException.FORBIDDEN) {
						return "forbidden";
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
	
	public static boolean verifyBucketLogging(String companyId, String userId) throws ServiceFailedException {

		String bucketName = MediaLibraryService.getBucketName(companyId);

		Storage storage;

		try {
			storage = StorageHelper.getStorage(userId);
		
		    Storage.Buckets.Get getBucket = storage.buckets().get(bucketName);
		    getBucket.setProjection("noacl");
		    Bucket bucket = getBucket.execute();
		    
		    if (bucket.getLogging() == null || !Globals.LOGS_BUCKET_NAME.equals(bucket.getLogging().getLogBucket())) {
		    	
//			    Storage.Buckets.Update updateBucket = storage.buckets().update(bucketName, bucket);
//			    
//			    Bucket.Logging logging = new Bucket.Logging();
//			    logging.setLogBucket(Globals.LOGS_BUCKET_NAME);
//			    logging.setLogObjectPrefix(bucketName);
//			
//			    bucket.setLogging(logging);
//			    
//			    updateBucket.execute();
		
				return false;
		
		    }
		    
			return true;
		
	    } catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() != ServiceFailedException.NOT_FOUND) {
				log.warning(e.getStatusCode() + " - " + e.getMessage());
			}
	    	
			throw new ServiceFailedException(e.getStatusCode());
			
//	        GoogleJsonError error = e.getDetails();
//
//	        log.warning("Error code: " + error.getCode());
//			log.warning("Error message: " + error.getMessage());
	        // More error information can be retrieved with error.getErrors().
	      } catch (HttpResponseException e) {
	        // No Json body was returned by the API.
	        log.warning("HTTP Status code: " + e.getStatusCode());
	        log.warning("HTTP Reason: " + e.getMessage());
	        
	        throw new ServiceFailedException(e.getStatusCode());
	        
	      } catch (IOException e) {
	        // Other errors (e.g connection timeout, etc.).
	    	log.warning("An error occurred: " + e);
	    	
	    	throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
	      }
    
	}
	
//	public static boolean verifyBucketLogging(String companyId) throws ServiceFailedException {
//		boolean enabled = false;
//
//		String bucketName = MediaLibraryService.getBucketName(companyId);
//
//		enabled = checkBucketLogging(bucketName);
//
//		if (!enabled) {
//			updateBucketLogging(bucketName, true);
//			
//			return false;
//		}
//		
//		return true;
//	}

//	private static boolean checkBucketLogging(String bucketName) throws ServiceFailedException {
//		MediaLibraryService service = MediaLibraryService.getInstance();
//
//		try {
//			InputStream stream = service.getBucketProperty(bucketName, "logging");
//			if (stream != null) {
//				LoggingResponse loggingResponse = new LoggingResponse(stream);
//				
//				return loggingResponse.getLogging();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return false;
//	}
//	
//	private static void updateBucketLogging(String bucketName, boolean enabled) throws ServiceFailedException {
//		MediaLibraryService service = MediaLibraryService.getInstance();
//
//		try {
//			String propertyXMLdoc = enabled ? LOGGING_ENABLED_XML.replace("%bucketName%", bucketName) : LOGGING_DISABLED_XML;
//			
//			service.updateBucketProperty(bucketName, "logging", propertyXMLdoc);
//		} catch (ServiceFailedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}

