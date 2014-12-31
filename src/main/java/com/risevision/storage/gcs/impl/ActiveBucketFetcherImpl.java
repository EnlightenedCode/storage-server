package com.risevision.storage.gcs.impl;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.queue.tasks.BQUtils;

public class ActiveBucketFetcherImpl implements ActiveBucketFetcher {
  private static Bigquery bqClient = BQUtils.getBigquery();
  private static final Logger log = Logger.getAnonymousLogger();

  @Override
  public QueryResponse getResults(int days, Long maxResults) throws IOException {
    log.info("Fetching active buckets");
    if (maxResults == null) {maxResults = 50L;}

    Long fromTS = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000;

    String query = "select substr(cs_bucket,18) as companyid from " + Globals.DATASET_ID + ".UsageLogs where cs_bucket is not null and time_timestamp > " + fromTS * 1000 + " group by companyid";

    log.info("Fetching active buckets using query: " + query);

    return bqClient.jobs().query
    (Globals.PROJECT_ID, new QueryRequest().setQuery(query).setMaxResults(maxResults))
    .execute();
  }

  public GetQueryResultsResponse getPagedResults(String jobId, Long maxResults, String pageToken)
  throws IOException {
    log.info("Fetching active buckets from previous result");
    if (maxResults == null) {maxResults = 50L;}
    if (maxResults > 100) {throw new IllegalArgumentException("Queue limit: 100");}
    if (jobId == null) {throw new IllegalArgumentException("missing jobid");}
    if (pageToken == null) {throw new IllegalArgumentException("missing pageToken");}

    return bqClient.jobs().getQueryResults(Globals.PROJECT_ID, jobId)
    .setPageToken(pageToken)
    .setMaxResults(maxResults)
    .execute();
  }
}
