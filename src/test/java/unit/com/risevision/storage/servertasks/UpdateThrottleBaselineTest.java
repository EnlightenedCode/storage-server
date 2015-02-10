package com.risevision.storage.servertasks;

import java.io.IOException;
import java.io.Closeable;

import com.risevision.storage.servertasks.UpdateThrottleBaselineServerTask;
import com.risevision.storage.BigqueryResponseRequestor;
import com.risevision.storage.MockBigqueryResponseRequestor;
import com.risevision.storage.Globals;
import com.risevision.storage.datastore.OfyService;
import com.risevision.storage.entities.ThrottleBaseline;

import com.google.appengine.tools.development.testing.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class UpdateThrottleBaselineTest {
  @Test public void itCallsBigquery() throws IOException {
    MockBigqueryResponseRequestor bqRequestor = new MockBigqueryResponseRequestor();

    UpdateThrottleBaselineServerTask task = new UpdateThrottleBaselineServerTask
    (null, null, bqRequestor);

    task.getDataFromBQ();

    assertThat ("it made the correct call",
    bqRequestor.getQuery(),
    equalTo(Globals.THROTTLE_BASELINE_QUERY));

    assertThat ("it made the correct call",
    bqRequestor.getProjectId(),
    equalTo(Globals.PROJECT_ID));
  }

  @Test public void itRetrievesDataFromBigQuery() throws IOException {
    MockBigqueryResponseRequestor bqRequestor = new MockBigqueryResponseRequestor();
    bqRequestor.addResponsePage(new Object[][] {{"20"}, {"17"}, {"15"}});
    bqRequestor.addResponsePage(new Object[][] {{"10"}, {"9"}, {"7"}});

    UpdateThrottleBaselineServerTask task = new UpdateThrottleBaselineServerTask
    (null, null, bqRequestor);

    task.getDataFromBQ();
    assertThat("it has the result", task.countsFromQuery.length, is(4));
    assertThat("it has the result", task.countsFromQuery[0], is(20d));
    assertThat("it has the result", task.countsFromQuery[2], is(15d));
    assertThat("it has the result", task.countsFromQuery[3], is(10d));
  }

  @Test public void itCalculatesMeanAndSD() throws IOException {
    MockBigqueryResponseRequestor bqRequestor = new MockBigqueryResponseRequestor();
    bqRequestor.addResponsePage(new Object[][] {{"10"}, {"7"}, {"4"}});
    bqRequestor.addResponsePage(new Object[][] {{"3"}, {"3"}});

    UpdateThrottleBaselineServerTask task = new UpdateThrottleBaselineServerTask
    (null, null, bqRequestor);

    task.getDataFromBQ();
    task.calculateMeanAndSD();

    assertThat("it set the correct mean", 
    task.countsMean,
    equalTo((double)(10 + 7 + 4)/3));

    assertThat("it set the correct standard deviation", 
    task.countsSD,
    equalTo(3.0));
  }

  @Test public void itPersistsData() throws IOException {
    LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    helper.setUp();
    Closeable session = OfyService.begin();

    MockBigqueryResponseRequestor bqRequestor = new MockBigqueryResponseRequestor();
    bqRequestor.addResponsePage(new Object[][] {{"10"}, {"7"}, {"4"}});
    bqRequestor.addResponsePage(new Object[][] {{"3"}, {"3"}});

    UpdateThrottleBaselineServerTask task = new UpdateThrottleBaselineServerTask
    (null, null, bqRequestor);

    task.getDataFromBQ();
    task.calculateMeanAndSD();
    task.saveData();

    assertThat("it saved the mean", 
    OfyService.ofy().load().type(ThrottleBaseline.class).order("-date").limit(1).first().now().getMean(),
    equalTo(task.countsMean));

    assertThat("it saved the standard deviation", 
    OfyService.ofy().load().type(ThrottleBaseline.class).order("-date").limit(1).first().now().getSD(),
    equalTo(task.countsSD));

    assertThat("it saved the minimum value", 
    OfyService.ofy().load().type(ThrottleBaseline.class).order("-date").limit(1).first().now().getMin(),
    equalTo(4d));

    assertThat("it saved the maximum value", 
    OfyService.ofy().load().type(ThrottleBaseline.class).order("-date").limit(1).first().now().getMax(),
    equalTo(10d));

    session.close();
    helper.tearDown();
  }
}
