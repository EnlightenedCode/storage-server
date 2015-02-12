package com.risevision.storage.servertasks;

import java.io.IOException;
import java.io.Closeable;
import java.util.*;

import com.risevision.storage.servertasks.SaveToDatastoreOffendersHandler;
import com.risevision.storage.datastore.OfyService;
import com.risevision.storage.entities.RvStorageObject;

import com.google.appengine.tools.development.testing.*;
import com.google.api.services.bigquery.model.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import com.google.api.services.bigquery.model.*;

public class SaveToDatastoreOffendersHandlerTest {
  LocalServiceTestHelper helper;
  Closeable session;

  private TableCell makeCell(Object o) { 
    TableCell cell = new TableCell();
    cell.setV(o);
    return cell;
  }

  private TableRow makeRow(Object... rowElements) {
    TableRow row = new TableRow();
    List<TableCell> cells = new ArrayList<>();
    for (Object rowElement : rowElements) {
      cells.add(makeCell(rowElement));
    }
    row.setF(cells);
    return row;
  }

  @Before public void setupDatastore() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
    .setApplyAllHighRepJobPolicy());

    helper.setUp();
    session = OfyService.begin();
  }

  @After public void tearDownDatastore() throws IOException {
    session.close();
    helper.tearDown();
  }


  @Test public void itSavesRecords() {
    SaveToDatastoreOffendersHandler handler = new SaveToDatastoreOffendersHandler();
    List<TableRow> offenders = new ArrayList<>();
    
    offenders.add(makeRow("ip", "referer", "cid", "file1", "99999"));
    offenders.add(makeRow("ip", "referer", "cid", "file1", "99999"));
    offenders.add(makeRow("ip", "referer", "cid", "file2", "77777"));
    handler.handle(offenders);

    assertThat("It saved the record without duplicating.",
    OfyService.ofy().load().type(RvStorageObject.class)
    .filter("companyId", "cid").filter("objectId", "file1").list().size(),
    equalTo(1));

    Integer countVal = OfyService.ofy().load().type(RvStorageObject.class)
    .id("cidfile2").now().getThrottleOffenderCounts().get(0);

    assertThat("It saved with the proper count.",
    countVal,
    equalTo(77777));
  }
}
