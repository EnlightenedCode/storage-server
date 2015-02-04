package com.risevision.storage.servertasks.impl;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.info.ServiceFailedException;
import com.google.api.client.util.GenericData;
import com.google.api.services.bigquery.model.*;
import com.google.common.collect.ImmutableList;

public class ActiveBucketFetcherMock implements ActiveBucketFetcher {
  private String[] companies = {
  "22e2ea3f-c4ab-4747-88ef-e6c3bddec195",
  "2543654c-c26f-444d-942f-ab390a6d92e5",
  "2cbb7001-16d8-44dd-a444-e22f811b3bd7"};

  @Override
  public QueryResponse getResults(int days, Long maxResults) throws IOException {
    QueryResponse result = new QueryResponse();
    List<TableRow> rowList = new ArrayList<>();

    for (String company : companies) {
      TableCell cell = new TableCell().setV(company);
      rowList.add(new TableRow().setF(ImmutableList.of(cell)));
    }

    result.setKind("bigquery#queryResponse");
    result.setJobComplete(true);
    result.setRows(rowList);
    
    return result;
  }

  public GetQueryResultsResponse 
  getPagedResults(String jobId, Long maxResults, String pageToken) throws IOException {
    return new GetQueryResultsResponse();
  }
}
