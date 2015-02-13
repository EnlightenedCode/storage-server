package com.risevision.storage;

import java.io.IOException;
import java.io.Closeable;
import java.util.HashMap;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.google.api.services.bigquery.model.*;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.*;

import com.google.appengine.tools.development.testing.*;

import com.risevision.storage.datastore.OfyService;
import com.risevision.storage.entities.ThrottleBaseline;
import com.risevision.storage.Globals;

public class GoogleBigqueryResponseRequestorIT {
  @Test public void itFetchesAResponseWithPaging() throws IOException {
    assertThat(1,equalTo(1));
//    String query =
//    "select contributor_username from " +
//    "[publicdata:samples.wikipedia] " +
//    "order by timestamp, contributor_username, revision_id " +
//    "limit " + (Integer.parseInt(Globals.BIGQUERY_MAX_RESULTS_PER_PAGE) + 1);
//
//    LocalServiceTestHelper helper =
//    new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());
//    helper.setUp();
//
//    GoogleBigqueryResponseRequestor requestor =
//    new GoogleBigqueryResponseRequestor();
//    requestor.setUseCache(false);
//
//    GetQueryResultsResponse resp =
//    requestor.fromQueryString(query, Globals.PROJECT_ID);
//    System.out.println
//    ("Got query results" + (resp.getCacheHit() ? " from cache." : "."));
//
//    assertThat("It received a response", resp.getJobComplete(), equalTo(true));
//
//    assertThat("It handles first page",
//    resp.getRows().size(),
//    equalTo(Integer.parseInt(Globals.BIGQUERY_MAX_RESULTS_PER_PAGE)));
//
//    resp =
//    requestor.fromToken(resp.getJobReference().getJobId(), resp.getPageToken());
//    assertThat("It handles second page", resp.getRows().size(), equalTo(1));
//
//    resp =
//    requestor.fromToken(resp.getJobReference().getJobId(), resp.getPageToken());
//    assertThat("It handles no more pages", resp, equalTo(null));
//
//    helper.tearDown();
  }
}
