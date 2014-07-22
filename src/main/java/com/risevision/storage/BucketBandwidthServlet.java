package com.risevision.storage;
import com.risevision.storage.queue.tasks.BQUtils;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.bigquery.*;
import com.google.api.services.bigquery.model.*;

import com.google.appengine.api.taskqueue.QueueFactory;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

public class BucketBandwidthServlet extends HttpServlet {
  private static final Logger log = Logger.getAnonymousLogger();
  private static final String BUCKET_BANDWIDTH_MONTH_QUERY = "select" +
   " substr(cs_bucket, 18) as bucket, sum(sc_bytes) as bytes_this_month from" +
   " " + Globals.DATASET_ID +".UsageLogs where" +
   " month(time_timestamp) = month(current_timestamp()) and" +
   " year(time_timestamp) = year(current_timestamp()) group by" +
   " bucket";
  private static final String TARGET_DATA_TABLE = "BucketBandwidthMonthly";

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response) throws IOException {
    log.info("Running bucket bandwidth query.");
    String queryId = BQUtils.startQuery(BUCKET_BANDWIDTH_MONTH_QUERY
                                       ,TARGET_DATA_TABLE
                                       ,BQUtils.WRITE_DISPOSITION_TRUNCATE);

    log.info("Adding memcache job to default task queue.");
    QueueFactory.getDefaultQueue().add(withUrl("/bucketBandwidthMemcache")
                                      .param("bqJob", queryId));
  }
}
