package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.QueryResponse;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.storage.Storage;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.risevision.storage.Globals;
import com.risevision.storage.api.SubscriptionStatusFetcher;
import com.risevision.storage.api.impl.SubscriptionStatusFetcherImpl;
import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.gcs.impl.ActiveBucketFetcherImpl;
import com.risevision.storage.info.ServiceFailedException;

public class LockDownUnpaidBucketsServerTask extends BatchServerTask {
  private ActiveBucketFetcher activeBucketFetcher;
  private SubscriptionStatusFetcher statusFetcher;
  private int days;
  
  public LockDownUnpaidBucketsServerTask(Storage client, Map<String, String[]> params) throws IOException {
    this(client, new ActiveBucketFetcherImpl(), new SubscriptionStatusFetcherImpl(), params);
  }

  public LockDownUnpaidBucketsServerTask(Storage client, ActiveBucketFetcher activeBucketFetcher, SubscriptionStatusFetcher statusFetcher, Map<String, String[]> params) throws IOException {
    super(client, params);
    
    confirmURLParams("days");
    
    this.activeBucketFetcher = activeBucketFetcher;
    this.statusFetcher = statusFetcher;
    this.days = new Integer(params.get("days")[0]);
  }

  @Override
  void handleRequest() throws IOException {
    List<TaskOptions> taskList = new ArrayList<>();
    List<TableRow> rows;

    if (pageToken == null) {
      listResult = activeBucketFetcher.getResults(days, maxResults);
      rows = ((QueryResponse)listResult).getRows();
    } else {
      listResult = activeBucketFetcher.getPagedResults
      (requestParams.get("jobId")[0], maxResults, pageToken);
      rows = ((GetQueryResultsResponse)listResult).getRows();
    }

    if (rows == null) {
      log.info("No active buckets");
      return;
    }

    for(TableRow row : rows) {
      String companyId = (String) row.getF().get(0).getV();

      try {
        SubscriptionStatus status = statusFetcher.getSubscriptionStatus(companyId);
        if(!status.isActive()) {
          taskList.add(makeTask(companyId));
        }
      } catch (ServiceFailedException e) {
        throw new IOException("SubscriptionStatusFetcher error");
      }
    }
    
    QueueFactory.getQueue("storageBulkOperations").add(taskList);

    submitNextTask();
  }

  private TaskOptions makeTask(String companyId) {
    return TaskOptions.Builder.withUrl("/servertask")
    .method(TaskOptions.Method.valueOf("GET"))
    .param("task", "RemovePublicReadBucket")
    .param("bucket", Globals.COMPANY_BUCKET_PREFIX + companyId);
  }
}
