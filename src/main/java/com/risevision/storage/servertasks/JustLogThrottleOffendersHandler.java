package com.risevision.storage.servertasks;

import java.util.logging.Logger;
import java.util.List;
import com.google.api.services.bigquery.model.*;

class JustLogThrottleOffendersHandler implements ThrottleOffendersHandler {
  private static final Logger log = Logger.getAnonymousLogger();

  public void handle(RvStorageObjectRowProcessor rp) {
    log.info("Offending files count: " + rp.getRvStorageObjects().size());
  }
}
