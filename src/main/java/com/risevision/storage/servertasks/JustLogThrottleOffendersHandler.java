package com.risevision.storage.servertasks;

import java.util.logging.Logger;
import java.util.List;
import com.google.api.services.bigquery.model.*;

class JustLogThrottleOffendersHandler implements ThrottleOffendersHandler {
  private static final Logger log = Logger.getAnonymousLogger();

  public void handle(List<TableRow> offenders) {
    log.info("Offending files count: " + offenders.size());
  }
}
