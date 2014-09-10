package com.risevision.storage;
import com.risevision.storage.queue.tasks.BQUtils;
import com.risevision.storage.gcs.GCSClient;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.storage.Storage;
import java.util.Arrays;
import java.util.ArrayList;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.common.base.Strings;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Buckets;

import com.google.appengine.api.taskqueue.QueueFactory;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

public class TrialInitiatorServlet extends HttpServlet {
  private static final Logger log = Logger.getAnonymousLogger();
  private static final int STATUS_CONFLICT = 409;
  private static final int STATUS_OK = 200;
  private static final int PREFIX_LENGTH = 
    Globals.COMPANY_BUCKET_PREFIX.length();
  private final com.google.appengine.api.taskqueue.TaskOptions.Method queueMethod = 
  com.google.appengine.api.taskqueue.TaskOptions.Method.GET;

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response) throws IOException {
    log.info("Running trial initiator servlet");

    Storage storage = GCSClient.getStorageClient();

    try {
      Storage.Buckets.List listRequest;
      listRequest = storage.buckets().list(Globals.PROJECT_ID);
      listRequest.setMaxResults(50L);
      if (request.getParameter("pageToken") != null) {
        listRequest.setPageToken(request.getParameter("pageToken"));
      }

      Buckets listResponse;
      listResponse = listRequest.execute();
      for (Bucket bucket : listResponse.getItems()) {
        if (bucket.getName().length() > PREFIX_LENGTH &&
            bucket.getName().startsWith(Globals.COMPANY_BUCKET_PREFIX)) {
          verifySubscription(bucket.getName().substring(PREFIX_LENGTH));
        }
      }

      if (listResponse.getNextPageToken() != null) {
        QueueFactory.getDefaultQueue().add(withUrl("/initiateStoreTrials")
                    .param("pageToken", listResponse.getNextPageToken())
                    .method(queueMethod));
      }
    } catch (Exception e) {
      log.warning(e.getMessage());
      response.setStatus(STATUS_CONFLICT);
      return;
    }

    response.setStatus(STATUS_OK);
  }
  
  private void verifySubscription(String companyId)
  throws IOException {
    if (Strings.isNullOrEmpty(companyId)) {
      throw new IOException("No company");
    }

    //The store subscription api will create a trial if one is available
    URL url = new URL(Globals.SUBSCRIPTION_AUTH_URL + companyId);
    java.net.HttpURLConnection httpConn = 
      (java.net.HttpURLConnection)url.openConnection();
    httpConn.setInstanceFollowRedirects(false);

    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(httpConn.getInputStream()));

    String result = reader.readLine();
    log.info("Store auth result for " + companyId + ": " + result);
    reader.close();

    if (!result.contains("\"authorized\":true") && !result.contains("Company not found")) {
      throw new IOException("No auth - failed to create trial");
    }
  }
}

