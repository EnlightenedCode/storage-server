package com.risevision.storage.servertasks;

import java.util.*;
import java.io.IOException;
import java.math.BigInteger;

import com.risevision.storage.queue.tasks.BQUtils;
import com.risevision.storage.Globals;
import com.risevision.storage.BigqueryResponseRequestor;
import com.risevision.storage.GoogleBigqueryResponseRequestor;
import com.risevision.storage.entities.ThrottleBaseline;

import com.risevision.storage.datastore.DatastoreService;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import org.apache.commons.math3.stat.StatUtils;

class UpdateThrottleBaselineServerTask extends ServerTask {
  double[] countsFromQuery;
  double countsMean;
  double countsSD;

  private BigqueryResponseRequestor bqRequestor =
  new GoogleBigqueryResponseRequestor();

  UpdateThrottleBaselineServerTask(Storage client, Map<String, String[]> params)
  throws IOException {
    super(client, params);
  }

  UpdateThrottleBaselineServerTask(Storage client, Map<String, String[]> params, BigqueryResponseRequestor bqRequestor) throws IOException {
    super(client, params);
    this.bqRequestor = bqRequestor;
  }

  void handleRequest() throws IOException {
    if (forceQueue()) {
      callFromTaskQueue();
      return;
    }

    getDataFromBQ();
    calculateMeanAndSD();
    saveData();
    return;
  }

  boolean forceQueue() {
    return requestParams.containsKey("forceQueue");
  }

  void callFromTaskQueue() {
    TaskOptions options = TaskOptions.Builder.withUrl("/servertask");
    options.param("task", requestParams.get("task")[0]);
    options.method(TaskOptions.Method.valueOf("GET"));
    QueueFactory.getQueue("storageBulkOperations").add(options);
  }

  void getDataFromBQ() throws IOException {
    GetQueryResultsResponse resp = bqRequestor.fromQueryString
    (Globals.THROTTLE_BASELINE_QUERY, Globals.PROJECT_ID);

    if (resp == null) { return; }

    BigInteger totalRows = resp.getTotalRows();

    if (totalRows.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
      throw new RuntimeException("Table result is much too large."); 
    }

    countsFromQuery = new double[(int)Math.ceil(totalRows.intValue() * 0.6)];

    int i = 0;
    do {
      List<TableRow> tableRows = resp.getRows();
      if (tableRows == null) { return; }
      for (TableRow row : tableRows) {
        countsFromQuery[i] = Double.parseDouble(row.getF().get(0).getV().toString());
        i += 1;
        if (i == countsFromQuery.length) { break; }
      }

      resp = bqRequestor.fromToken
      (resp.getJobReference().getJobId(), resp.getPageToken());
    } while (resp != null && i < countsFromQuery.length);
  }

  void calculateMeanAndSD() {
    if (!confirmDescendingSort()) {
      throw new RuntimeException("Query did not return descending sorted results.");
    }

    this.countsMean = StatUtils.mean(countsFromQuery);
    this.countsSD = Math.sqrt(StatUtils.variance(countsFromQuery));
  }

  private boolean confirmDescendingSort() {
    for (int i = 0; i < countsFromQuery.length - 1; i += 1) {
      if (countsFromQuery[i] < countsFromQuery[i + 1]) {return false;}
    }
    return true;
  }

  void saveData() {
    DatastoreService.getInstance().put
    (new ThrottleBaseline(countsMean,
                          countsSD,
                          countsFromQuery[countsFromQuery.length - 1],
                          countsFromQuery[0]));
  }
}

