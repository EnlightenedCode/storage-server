package com.risevision.storage;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import com.risevision.storage.Globals;

public class ServletContextListener implements javax.servlet.ServletContextListener {
  private static final Logger log = Logger.getAnonymousLogger();

  @Override
  public void contextInitialized(ServletContextEvent event) {
    log.info("Initializing servlet.");
    if (Globals.devserver) {
      log.info("Running on local devserver");
    } else {
      log.info("Running on hosted GAE");
    }
    log.info("Using the following logging globals:");
    log.info(Globals.LOGGING_ENABLED_XML);
    log.info(Globals.LOGS_BUCKET_NAME);
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }
}
