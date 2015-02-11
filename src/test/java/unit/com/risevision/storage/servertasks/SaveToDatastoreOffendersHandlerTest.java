package com.risevision.storage.servertasks;

import java.io.IOException;
import java.io.Closeable;
import java.util.*;

import com.risevision.storage.servertasks.SaveToDatastoreOffendersHandler;
import com.risevision.storage.datastore.OfyService;

import com.google.appengine.tools.development.testing.*;
import com.google.api.services.bigquery.model.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import com.google.api.services.bigquery.model.*;

public class SaveToDatastoreOffendersHandlerTest {
  private TableCell makeCell(Object o) { 
    TableCell cell = new TableCell();
    cell.setV(o);
    return cell;
  }

  private TableRow makeRow(Object... o) {
    TableRow row = new TableRow();
    List<TableCell> cells = new ArrayList<>();
    for (Object obj in o) {
      cells.add(makeCell(o));
    }
    row.setF(cells);
    return row;
  }

  @Before public void setupDatastore() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

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
    
    cells.add(makeCell("ip"));
    cells.add(makeCell("referer"));
    cells.add(makeCell("bucket"));
    cells.add(makeCell("file"));
    cells.add(makeCell(999999999999d));

    offenders.add(makeRow("ip", "referer", "bucket", "file1", 999999999999d));
    offenders.add(makeRow("ip", "referer", "bucket", "file1", 999999999999d));
    offenders.add(makeRow("ip", "referer", "bucket", "file2", 777777777777d));
    handler.handle(offenders);

    assertThat("It saved the record.",
    OfyService.ofy().load().type(RvStorageObject.class)
    .filter("ObjectId", "file1").filter("bucket", "bucket").now().list().size()
    equalTo(1));

    assertThat("It saved the record.",
    OfyService.ofy().load().type(RvStorageObject.class).filter("ObjectId", "file2")
    .filter("bucket", "bucket").now().list().get(0).getCount()
    equalTo(777777777777d));
  }
}
