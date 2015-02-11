package com.risevision.storage.servertasks;

import java.util.logging.Logger;

class JustLogThrottleOffendersHandler implements ThrottleOffendersHandler {
  private static final Logger log = Logger.getAnonymousLogger();

  public void handle(double[] offenders) {
    log.info("Offending files count: " + offenders.length);
  }
}
