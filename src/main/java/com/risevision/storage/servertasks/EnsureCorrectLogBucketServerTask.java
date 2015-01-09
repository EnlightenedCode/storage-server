package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.GenericData;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageRequest;
import com.google.api.services.storage.model.Bucket;
import com.risevision.storage.Globals;
import com.risevision.storage.servertasks.requestupdater.LogStorageRequestUpdater;
import com.risevision.storage.servertasks.requestupdater.StorageRequestUpdater;

public class EnsureCorrectLogBucketServerTask extends BatchServerTask {
  public EnsureCorrectLogBucketServerTask(Storage gcsStorageClient, Map<String, String[]> params) throws IOException {
    super(gcsStorageClient, params);
  }

  public void handleRequest() throws IOException {
    log.info("Ensuring all buckets log to the correct log bucket");
    
    setListResult();
    prepareBatchRequest();
    submitBatchRequest();
    submitNextTask();
  }

  protected void setListResult() throws IOException {
    listRequest = new ListRequestGenerator(gcsClient).getRequest();
    listRequest.set("pageToken", pageToken);
    listRequest.set("maxResults", maxResults);
    
    listResult = (GenericData) listRequest.execute();
    listResult = filterBuckets(listResult);
  }
  
  @SuppressWarnings("unchecked")
  protected GenericData filterBuckets(GenericData listResult) throws IOException {
    GenericData returnResult = new GenericData();
    returnResult.set("kind", listResult.get("kind"));
    returnResult.set("nextPageToken", listResult.get("nextPageToken"));
    returnResult.set("prefixes", listResult.get("prefixes"));
    
    List<GenericData> returnItems = new ArrayList<>();
    
    for (GenericData item : (List<GenericData>) listResult.get("items")) {
      if(shouldProcessItem(item)) {
        returnItems.add(item);
      }
    }
    
    returnResult.set("items", returnItems);
    return returnResult;    
  }
  
  public boolean shouldProcessItem(GenericData item) throws IOException {
    Bucket bucket = (Bucket) item;
    
    if(!bucket.getName().startsWith(Globals.COMPANY_BUCKET_PREFIX)) {
      return false;
    }
    
    return (bucket.getLogging() == null || !Globals.LOGS_BUCKET_NAME.equals(bucket.getLogging().getLogBucket()));
  }
  
  @Override
  protected StorageRequestUpdater createStorageRequestUpdater(StorageRequest<?> iteratingRequest) {
    return new LogStorageRequestUpdater(getGcsClient());
  }
}
