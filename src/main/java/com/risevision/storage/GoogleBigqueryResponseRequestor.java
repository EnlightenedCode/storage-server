package com.risevision.storage;

import com.google.api.services.bigquery.model.*;
import com.google.api.services.bigquery.*;
import com.risevision.storage.queue.tasks.BQUtils;
import com.google.api.client.util.ExponentialBackOff.Builder;
import com.google.api.client.util.ExponentialBackOff;

import java.io.IOException;
import java.util.logging.Logger;

public class GoogleBigqueryResponseRequestor implements BigqueryResponseRequestor {
  private static final Logger log = Logger.getAnonymousLogger();
  private boolean useCache = true;

  public GetQueryResultsResponse fromQueryString(String query, String projectId)
  throws IOException {
    JobConfigurationQuery jcq = 
    new JobConfigurationQuery().setQuery(query).setUseQueryCache(useCache);

    JobConfiguration jc = new JobConfiguration().setQuery(jcq);

    Job job = new Job().setConfiguration(jc);

    job = BQUtils.getBigquery().jobs().insert(Globals.PROJECT_ID, job).execute();

    Bigquery.Jobs.GetQueryResults resultsQuery = BQUtils.getBigquery().jobs()
    .getQueryResults(Globals.PROJECT_ID, job.getJobReference().getJobId());

    return getQueryResults(resultsQuery);
  }

  public GetQueryResultsResponse fromToken(String jobId, String pageToken)
  throws IOException {
    if (pageToken == null) {return null;}
    Bigquery.Jobs.GetQueryResults resultsQuery = BQUtils.getBigquery().jobs()
    .getQueryResults(Globals.PROJECT_ID, jobId)
    .setPageToken(pageToken);

    return getQueryResults(resultsQuery);
  }

  private GetQueryResultsResponse getQueryResults
  (Bigquery.Jobs.GetQueryResults query) throws IOException {
    GetQueryResultsResponse resultsResponse;

    query.setMaxResults(Long.valueOf(Globals.BIGQUERY_MAX_RESULTS_PER_PAGE));
    query.setTimeoutMs(500L);

    ExponentialBackOff backoff = new ExponentialBackOff.Builder()
    .setMaxElapsedTimeMillis(50000)
    .setInitialIntervalMillis(1000)
    .build();
    long sleepTime;

    while (true) {
      log.info("Executing GetQueryResults.");
      resultsResponse = query.execute();
      if (resultsResponse.getJobComplete()) { break;}

      sleepTime = backoff.nextBackOffMillis();
      log.info("Job not complete.  Sleeping for " + sleepTime + "millis.");
      if (sleepTime == backoff.STOP) {
        throw new IOException("Request timed out.");
      }

      try {
      java.lang.Thread.sleep(sleepTime);
      } catch (java.lang.InterruptedException e) {
        throw new IOException("Thread interrupted.  Aborting.");
      }
    }

    return resultsResponse;
  }

  void setUseCache(boolean useCache) { this.useCache = useCache; }
}
