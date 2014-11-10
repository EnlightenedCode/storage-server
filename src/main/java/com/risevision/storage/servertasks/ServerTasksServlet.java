package com.risevision.storage.servertasks;

import com.risevision.storage.gcs.GCSClient;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.*;
import com.google.api.services.storage.Storage;

public class ServerTasksServlet extends HttpServlet {
  private static final Logger log = Logger.getAnonymousLogger();

  private static final int INTERNAL_SERVER_ERROR = 500;
  private static final int NOT_IMPLEMENTED = 501;
  private static final int STATUS_OK = 200;
  private static String packageName;

  Storage gcsStorageClient = GCSClient.getStorageClient();

  public void init() {
    packageName = this.getClass().getPackage().getName();
  }

  public void doGet
  (HttpServletRequest request, HttpServletResponse response) {
    log.info("Starting server task: " + request.getParameter("task"));

    try {
      String className = packageName + "." +
      request.getParameter("task") + "ServerTask";

      ((Class<ServerTask>) Class.forName(className))
      .getDeclaredConstructor(Storage.class, java.util.Map.class)
      .newInstance(gcsStorageClient, request.getParameterMap())
      .handleRequest();
    } catch (IOException e) {
      log.severe("Task error: " + e.toString());
      response.setStatus(INTERNAL_SERVER_ERROR);
      return;
    } catch (Exception e) {
      log.severe("Error starting task: " + e.toString());
      e.printStackTrace();
      response.setStatus(NOT_IMPLEMENTED);
      return;
    }

   response.setStatus(STATUS_OK);
  }
}
