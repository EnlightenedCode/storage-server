package com.risevision.storage.gcs;

import java.io.IOException;

import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.QueryResponse;

public interface ActiveBucketFetcher {
  public QueryResponse getResults(int days, Long maxResults) throws IOException;

  public GetQueryResultsResponse getPagedResults
  (String jobId, Long maxResults, String pageToken) throws IOException ;
}
