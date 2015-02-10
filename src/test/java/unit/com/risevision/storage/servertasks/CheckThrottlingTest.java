package com.risevision.storage.servertasks;

import java.io.IOException;
import java.io.Closeable;

import com.risevision.storage.servertasks.UpdateThrottleBaselineServerTask;
import com.risevision.storage.BigqueryResponseRequestor;
import com.risevision.storage.MockBigqueryResponseRequestor;
import com.risevision.storage.Globals;
import com.risevision.storage.datastore.OfyService;
import com.risevision.storage.entities.ThrottleBaseline;
import com.risevision.storage.datastore.DatastoreService;

import com.google.appengine.tools.development.testing.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class CheckThrottlingTest {
  LocalServiceTestHelper helper;
  Closeable session;

  @Before public void setupDatastore() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    helper.setUp();
    session = OfyService.begin();

    DatastoreService.getInstance().put(new ThrottleBaseline(7d, 30.9d, 1, 2000));
  }

  @After public void tearDownDatastore() throws IOException {
    session.close();
    helper.tearDown();
  }

  @Test public void itFetchesBaselineData() throws IOException {
    CheckThrottlingServerTask task = new CheckThrottlingServerTask
    (null, null, null);

    task.getBaselineData();

    assertThat("it has mean", task.baselineMean, equalTo(7d));
    assertThat("it has sd", task.baselineSD, equalTo(30.9d));
  }

  @Test public void itCallsBigquery() throws IOException {
    MockBigqueryResponseRequestor bqRequestor = new MockBigqueryResponseRequestor();

    CheckThrottlingServerTask task = new CheckThrottlingServerTask
    (null, null, bqRequestor);

    task.getBaselineData();
    task.getDataFromBQ();

    assertThat ("it made the correct call",
    bqRequestor.getQuery(),
    equalTo(Globals.THROTTLE_CHECK_QUERY.replace("BASELINE_COMPARISON", "99")));

    assertThat ("it made the correct call",
    bqRequestor.getProjectId(),
    equalTo(Globals.PROJECT_ID));
  }
}
