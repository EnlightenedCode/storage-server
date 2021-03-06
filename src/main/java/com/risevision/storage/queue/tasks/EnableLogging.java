package com.risevision.storage.queue.tasks;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.BucketAccessControl;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.common.base.Strings;
import com.risevision.storage.Globals;
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
	
	private static final String PROJECTION_NOACL = "noacl";
	private static final String PROJECTION_FULL = "full";
	
	private static final String STORAGE_ANALYTICS_EMAIL = "cloud-storage-analytics@google.com";
	private static final String SCOPE_WRITE = "WRITER";
	private static final String INDEX_ALL_COMPANIES = "directory.companies";
	
	private static Storage storage;

	private static int QUERY_LIMIT = 500;
	
	public static String runJob(String userId, String lastCompanyId) {
		int jobCount = 0, successCount = 0, failCount = 0;
		try {
			
			storage = StorageHelper.getStorage(userId);
			
		} catch (IOException e) {
			log.severe("Storage Initialization failed" + e.getMessage());
			
			return "Error";
		}
		
		// Job is starting, verify Logging bucket ACLs
		if (Strings.isNullOrEmpty(lastCompanyId)) {
			try {
				verifyLoggingBucketACL();
			} catch (ServiceFailedException e) {
				if (e.getReason() == ServiceFailedException.FORBIDDEN) {
					return "forbidden";
				}
			}

		}

		List<String> companyIds = getCompanyIDs(lastCompanyId);
		
		if (companyIds.isEmpty()) {
			return "Done";
		}
		
		log.info("First Company:" + companyIds.get(0));
		
		for (String companyId: companyIds) {
			boolean enabled = false;

			try {
				enabled = verifyBucketLogging(companyId);

//				log.info(String.format("Company %s has logging %s", companyId, enabled ? "enabled" : "disabled"));
				if (!enabled) {
					successCount++;
				}
				
			} catch (ServiceFailedException e) {
				if (e.getReason() == ServiceFailedException.FORBIDDEN || 
						e.getReason() == ServiceFailedException.AUTHENTICATION_FAILED) {
					log.severe(String.format("Verifying Company %s has failed. Error:%d", companyId, e.getReason()));

					return "Auth failed";
					
				}
				
				failCount++;
			}
			
			jobCount++;
			lastCompanyId = companyId;
			
		}
		
		QueueFactory.getQueue(QueueName.STORAGE_LOG_ENABLE).add(withUrl("/queue")
				.param(QueryParam.TASK, QueueTask.RUN_ENABLE_LOGGING_JOB)
				.param(QueryParam.JOB_CURSOR, lastCompanyId)
				.countdownMillis(1000 * 5)
				.method(Method.GET));
		
		log.info("Last Company:" + lastCompanyId);
		log.info("Processed:" + jobCount + " Modified:" + successCount + " Not found:" + failCount);
		
		return "queued next job";
			
	}
	
	private static boolean verifyLoggingBucketACL() throws ServiceFailedException {
		String bucketName = Globals.LOGS_BUCKET_NAME;
		
		try {
		    Storage.Buckets.Get getBucket = storage.buckets().get(bucketName);
		    getBucket.setProjection(PROJECTION_FULL);
		    Bucket bucket = getBucket.execute();
		    
		    for (BucketAccessControl acl: bucket.getAcl()) {
		    	
		    	if (acl.getEmail() != null && acl.getEmail().equals(STORAGE_ANALYTICS_EMAIL)) {
			    	log.info("Logs bucket configured correctly: " + bucketName);

		    		return true;
		    	}
		
		    }
		    
		    // ACL not found
		    Storage.Buckets.Update updateBucket = storage.buckets().update(bucketName, bucket);
		    
		    BucketAccessControl acl = new BucketAccessControl();
		    
		    acl.setBucket(bucketName);
		    acl.setEmail(STORAGE_ANALYTICS_EMAIL);
		    acl.setEntity("group-" + STORAGE_ANALYTICS_EMAIL);
		    acl.setRole(SCOPE_WRITE);
		    
		    bucket.getAcl().add(acl);
		    
		    updateBucket.execute();
		    
	    	log.info("Logs bucket ACL updated: " + bucketName);
			    
	    } catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() != ServiceFailedException.NOT_FOUND) {
				log.warning(e.getStatusCode() + " - " + e.getMessage());
			}
	    	
			throw new ServiceFailedException(e.getStatusCode());
		} catch (IOException e) {
	    	log.warning("An error occurred: " + e);
	    }
		
		return false;
	}
	
	private static List<String> getCompanyIDs(String lastId) {

		List<String> result = new ArrayList<String>();
		GetResponse<Document> response = null;
		
		String indexName = INDEX_ALL_COMPANIES;

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

		String bucketName = Globals.COMPANY_BUCKET_PREFIX + companyId;

		try {
			
		    Storage.Buckets.Get getBucket = storage.buckets().get(bucketName);
		    getBucket.setProjection(PROJECTION_NOACL);
		    Bucket bucket = getBucket.execute();
		    
		    if (bucket.getLogging() == null || !Globals.LOGS_BUCKET_NAME.equals(bucket.getLogging().getLogBucket())) {
		    	
			    Storage.Buckets.Update updateBucket = storage.buckets().update(bucketName, bucket);
			    
			    Bucket.Logging logging = new Bucket.Logging();
			    logging.setLogBucket(Globals.LOGS_BUCKET_NAME);
			    logging.setLogObjectPrefix(bucketName);
			
			    bucket.setLogging(logging);
			    
			    updateBucket.execute();
		
				return false;
		
		    }
		    
			return true;
		
	    } catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() != ServiceFailedException.NOT_FOUND) {
				log.warning(e.getStatusCode() + " - " + e.getMessage());
			}
	    	
			throw new ServiceFailedException(e.getStatusCode());
			
	      } catch (HttpResponseException e) {
	        log.warning("HTTP Status code: " + e.getStatusCode());
	        log.warning("HTTP Reason: " + e.getMessage());
	        
	        throw new ServiceFailedException(e.getStatusCode());
	        
	      } catch (IOException e) {
	        // Other errors (e.g connection timeout, etc.).
	    	log.warning("An error occurred: " + e);
	    	
	    	throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
	      }
    
	}
	
}

