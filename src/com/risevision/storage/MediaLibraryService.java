package com.risevision.storage;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;

public abstract class MediaLibraryService {
	private static MediaLibraryService instance; 
	/** Global instance of the HTTP transport. */
	protected static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	protected static final Logger log = Logger.getAnonymousLogger();

	public static MediaLibraryService getInstance() {
		try {
			if (instance == null)
				instance = new MediaLibraryServiceImpl();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public abstract ListAllMyBucketsResponse getAllMyBuckets() throws ServiceFailedException;
	
	public abstract InputStream getBucketProperty(String bucketName, String property) throws ServiceFailedException;
	
	public abstract String getBucketPropertyString(String bucketName, String property) throws ServiceFailedException;
	
	public List<MediaItemInfo> getBucketItems(String bucketName) throws ServiceFailedException {
		return getBucketItems(bucketName, null);
	}
	
	public abstract List<MediaItemInfo> getBucketItems(String bucketName, String prefix) throws ServiceFailedException;
	
	public String getBucketItemsString(String bucketName) throws ServiceFailedException {
		return getBucketItemsString(bucketName, null);
	}
	
	public String getBucketItemsString(String bucketName, String prefix) throws ServiceFailedException {
		String response = "";

		try {
			JSONWriter stringer;
			stringer = new JSONStringer();
			
//			getBucketProperty(bucketName, "logging");
//			getBucketProperty(bucketName, "lifecycle");

			List<MediaItemInfo> items = getBucketItems(bucketName, prefix);
			stringer.object();
			stringer.key("status").value(ServiceFailedException.OK);
			stringer.key("mediaFiles");
			stringer.array();
			for (MediaItemInfo item: items) {
				stringer.object();
				
				stringer.key("key").value(item.getKey());
				stringer.key("lastModified").value(item.getLastModified() != null ? item.getLastModified().getTime() : "");
//				stringer.key("sizeString").value(item.getSizeString());
				stringer.key("size").value(item.getSize());
				stringer.key("eTag").value(item.geteTag());
				
				stringer.endObject();
			}
			stringer.endArray();
			stringer.endObject();

//			ListAllMyBucketsResponse allbuckets = getAllMyBuckets();
//			stringer.array();
//			int count = 0; 
//			if (allbuckets != null) {
//				for (Object item: allbuckets.entries) {
//					Bucket bucket = (Bucket) item;
//					
//					stringer.object();
//
//					count++;
//					stringer.key("count").value(count);
//					stringer.key("name").value(bucket.name);
//					stringer.key("creationDate").value(bucket.creationDate);
//					
//					stringer.endObject();
//				}
//			}
//			stringer.endArray();
			
			response = stringer.toString();		

//			response = getBucketPropertyString(bucketName, "logging");
//
//			response += "\n";
//			response += Boolean.toString(MediaLibraryLogReader.verifyBucketLogging(bucketName));
//			response += "\n";
//			
//			response += getBucketPropertyString(bucketName, "logging");
			

		} catch (JSONException e) {
			log.severe("Error - " + e.getMessage());

			e.printStackTrace();
		}
		
		return response;
	}
	
	public abstract void createBucket(String bucketName) throws ServiceFailedException;
	
	public abstract void updateBucketProperty(String bucketName, String property, String propertyXMLdoc) throws ServiceFailedException;
	
	public abstract void deleteMediaItem(String bucketName, String itemName) throws ServiceFailedException;
	
	public abstract void deleteMediaItems(String bucketName, List<String> files) throws ServiceFailedException;
	
	public abstract InputStream getMediaItem(String bucketName, String itemName) throws ServiceFailedException;
	
	public abstract String getMediaItemUrl(String bucketName, String itemName) throws Exception;
	
	public abstract String getSignedPolicy(String policyBase64);

}
