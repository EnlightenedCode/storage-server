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
import com.risevision.storage.datastore.OfyService;

import com.google.api.services.storage.*;
import com.google.api.services.storage.model.*;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import org.apache.commons.math3.stat.StatUtils;

class CheckThrottlingServerTask extends ServerTask {
  final int ACCEPTABLE_DEVIATIONS = 3;
  double baselineMean;
  double baselineSD;
  double[] offenders;

  private BigqueryResponseRequestor bqRequestor =
  new GoogleBigqueryResponseRequestor();

  CheckThrottlingServerTask(Storage client, Map<String, String[]> params)
  throws IOException {
    super(client, params);
  }

  CheckThrottlingServerTask(Storage client, Map<String, String[]> params, BigqueryResponseRequestor bqRequestor) throws IOException {
    super(client, params);
    this.bqRequestor = bqRequestor;
  }

  void handleRequest() throws IOException {
    getBaselineData();
    getDataFromBQ();
    //saveData();
    return;
  }

  void getBaselineData() {
    ThrottleBaseline baseline = OfyService.ofy().load().type(ThrottleBaseline.class).order("-date").limit(1).first().now();

    baselineMean = baseline.getMean();
    baselineSD = baseline.getSD();
  }

  void getDataFromBQ() throws IOException {
    String query = Globals.THROTTLE_CHECK_QUERY;
    int comparison = (int)(baselineMean + ACCEPTABLE_DEVIATIONS * baselineSD);
    
    query = query.replace("BASELINE_COMPARISON", String.valueOf(comparison));

    GetQueryResultsResponse resp = bqRequestor.fromQueryString
    (query, Globals.PROJECT_ID);

    if (resp == null) { return; }

    BigInteger totalRows = resp.getTotalRows();

    if (totalRows.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
      throw new RuntimeException("Table result is much too large."); 
    }

    log.info("Throttle candidate count: " + totalRows.toString());
  }
}
