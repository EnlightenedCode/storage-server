package com.risevision.storage;

import com.risevision.storage.Globals;
import com.google.api.services.bigquery.model.*;
import java.math.BigInteger;
import java.util.*;
import java.io.IOException;

public class MockBigqueryResponseRequestor implements BigqueryResponseRequestor {
  private String query;
  private String projectId;
  private List<Object[][]> responsePages = new ArrayList<>();
  private int totalRows = 0;

  public GetQueryResultsResponse fromQueryString(String query, String projectId)
  throws IOException {
    this.query = query;
    this.projectId = projectId;

    return fromToken("x", "0");
  }

  public GetQueryResultsResponse fromToken(String jobId, String pageToken)
  throws IOException{
    if (pageToken == null) {return null;}

    GetQueryResultsResponse resp = new GetQueryResultsResponse();
    resp.setJobComplete(true);
    resp.setJobReference(new JobReference().setJobId("x").setProjectId(projectId));
    resp.setKind("bigquery#queryResponse");
    resp.setTotalRows(BigInteger.valueOf(this.totalRows));

    int pageIdx = Integer.parseInt(pageToken);
    if (pageIdx >= responsePages.size()) { return resp; }

    List<TableRow> rows = new ArrayList();
    Object[][] responsePageRows = responsePages.get(Integer.parseInt(pageToken));

    for (int i = 0; i < responsePageRows.length; i += 1) {
      rows.add(makeRow(responsePageRows[i]));
    }

    resp.setRows(rows);

    resp.setPageToken(null);
    if (responsePages.size() > Integer.parseInt(pageToken) + 1) {
      resp.setPageToken(Integer.toString(Integer.parseInt(pageToken) + 1));
    }

    return resp;
  }

  public void addResponsePage(Object[][] responseRows) {
    responsePages.add(responseRows);
    totalRows += responseRows.length;
  }
  
  private TableRow makeRow(Object[] rowCells) {
    TableRow tableRow = new TableRow();
    List<TableCell> cells = new ArrayList<>();

    for (int i = 0; i < rowCells.length; i += 1) {
      cells.add(new TableCell().setV(rowCells[i]));
    }

    tableRow.setF(cells);
    return tableRow;
  }

  public String getQuery() {
    return query;
  }

  public String getProjectId() {
    return projectId;
  }
}


