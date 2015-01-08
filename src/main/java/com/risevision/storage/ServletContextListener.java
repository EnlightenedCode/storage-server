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
    log.info("Using storage project: " + Globals.STORAGE_APP_NAME);
    log.info("Storage project id: " + Globals.PROJECT_ID);
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }
}
