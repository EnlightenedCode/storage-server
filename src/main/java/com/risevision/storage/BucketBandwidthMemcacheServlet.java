package com.risevision.storage;
import com.risevision.storage.queue.tasks.BQUtils;

import java.io.IOException;
import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.bigquery.model.*;
import com.google.appengine.api.memcache.*;

public class BucketBandwidthMemcacheServlet extends HttpServlet {
  private static final Logger log = Logger.getAnonymousLogger();
  private static final String SOURCE_DATA_TABLE = "BucketBandwidthMonthly";
  private static final int STATUS_CONFLICT = 409;
  private static final int STATUS_OK = 200;

  public void doPost(HttpServletRequest request,
                    HttpServletResponse response) throws IOException {
    log.info("Checking big query job status.");
    Job bqJob = BQUtils.getJob(request.getParameter("bqJob"));
    if (bqJob.getStatus().getState().equals("DONE")) {
      updateMemcache();
      response.setStatus(STATUS_OK);
    } else {
      log.info("Bigquery job not complete.");
      response.setStatus(STATUS_CONFLICT);
    }
  }

  private void updateMemcache() {
    List<TableRow> tableRows;
    HashMap<String, String> tableMap = new HashMap<String, String>(5000);
    log.info("Updating memcache with bucket bandwidth values.");

    tableRows = BQUtils.getTableRows(SOURCE_DATA_TABLE);
    for (TableRow row : tableRows) {
      List<TableCell> cells = row.getF();
      tableMap.put((String)cells.get(0).getV(), (String)cells.get(1).getV());
    } 

    MemcacheService syncCache = MemcacheServiceFactory
                               .getMemcacheService("month-bucket-bandwidth");
    syncCache.setErrorHandler(ErrorHandlers
                             .getConsistentLogAndContinue(WARNING));
    syncCache.putAll(tableMap);
  }
}
