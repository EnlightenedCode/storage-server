package com.risevision.storage;

import com.google.api.services.bigquery.model.GetQueryResultsResponse;

import java.io.IOException;

public interface BigqueryResponseRequestor {
  public GetQueryResultsResponse fromQueryString(String query, String projectId)
  throws IOException;

  public GetQueryResultsResponse fromToken(String jobId, String token)
  throws IOException;
}
