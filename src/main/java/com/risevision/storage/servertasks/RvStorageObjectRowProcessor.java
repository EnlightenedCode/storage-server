package com.risevision.storage.servertasks;

import java.util.*;
import com.google.api.services.bigquery.model.*;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.entities.RvStorageObject;

class RvStorageObjectRowProcessor {
  private final static int CID_COLUMN = 2;
  private final static int FILE_COLUMN = 3;
  private final static int IP_COLUMN = 0;
  private final static int REFERER_COLUMN = 1;
  private final static int COUNT_COLUMN = 4;
  private final String THROTTLE_TYPE;

  private HashMap<String, RvStorageObject> rvStorageObjectsMap;
  private List<TableRow> rows;

  RvStorageObjectRowProcessor(List<TableRow> rows, String throttleType) {
    this.rows = rows;
    this.THROTTLE_TYPE = throttleType;
    rvStorageObjectsMap = new HashMap<>();
    groupRowsByFileAsRvStorageObjects();
  }

  private void groupRowsByFileAsRvStorageObjects() {
    RvStorageObject rvStorageObject;

    for (TableRow fileRow : rows) {
      List<TableCell> cells = fileRow.getF();
      String companyId = (String)cells.get(CID_COLUMN).getV();
      String file = (String)cells.get(FILE_COLUMN).getV();

      rvStorageObject = rvStorageObjectsMap.get(companyId + file);
      if (rvStorageObject == null) {
        rvStorageObject = new RvStorageObject(companyId + file);
        rvStorageObject.setCompanyId(companyId);
        rvStorageObject.setObjectId(file);
      }

      rvStorageObject.addThrottleOffender
      (THROTTLE_TYPE,
      (String)cells.get(IP_COLUMN).getV(),
      (String)cells.get(REFERER_COLUMN).getV(),
      Integer.valueOf((String)cells.get(COUNT_COLUMN).getV()));

      rvStorageObjectsMap.put(rvStorageObject.getId(), rvStorageObject);
    }
  }

  public Collection<RvStorageObject> getRvStorageObjects() {
    return rvStorageObjectsMap.values();
  }

  public Set<String> getRvStorageKeys() {
    return rvStorageObjectsMap.keySet();
  }

  public List<TableRow> getTableRows() {
    return rows;
  }
}
