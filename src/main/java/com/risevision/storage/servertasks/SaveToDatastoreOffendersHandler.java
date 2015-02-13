package com.risevision.storage.servertasks;

import java.util.*;
import com.google.api.services.bigquery.model.*;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.entities.RvStorageObject;
import com.risevision.storage.datastore.OfyService;
import com.googlecode.objectify.VoidWork;

class SaveToDatastoreOffendersHandler implements ThrottleOffendersHandler {
  private final static int CID_COLUMN = 2;
  private final static int FILE_COLUMN = 3;
  private final static int IP_COLUMN = 0;
  private final static int REFERER_COLUMN = 1;
  private final static int COUNT_COLUMN = 4;
  private final static String THROTTLE_TYPE = "hourly-get";
  RvStorageObject rvStorageObj;

  HashMap<String, RvStorageObject> rvStorageObjects = new HashMap<>();

  public void handle(List<TableRow> offenders) {
    createOneRvStorageObjectPerFile(offenders);
    insertRvStorageObjectsIntoDatastore();
  }

  private void createOneRvStorageObjectPerFile(List<TableRow> offenders) {
    RvStorageObject rvStorageObject;

    for (TableRow offender : offenders) {
      List<TableCell> cells = offender.getF();
      String companyId = (String)cells.get(CID_COLUMN).getV();
      String file = (String)cells.get(FILE_COLUMN).getV();

      rvStorageObject = rvStorageObjects.get(companyId + file);
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

      rvStorageObjects.put(rvStorageObject.getId(), rvStorageObject);
    }
  }

  private void insertRvStorageObjectsIntoDatastore() {
    for (RvStorageObject rvStorageObject : rvStorageObjects.values()) {
      rvStorageObj = rvStorageObject;
      OfyService.ofy().transact(new VoidWork() { public void vrun() {
        RvStorageObject datastoreRec = OfyService.ofy().load()
        .type(RvStorageObject.class).id(rvStorageObj.getId()).now();

        if (datastoreRec == null) {
          datastoreRec = new RvStorageObject(rvStorageObj.getId());
          datastoreRec.setCompanyId(rvStorageObj.getCompanyId());
          datastoreRec.setObjectId(rvStorageObj.getObjectId());
        }

        datastoreRec.clearThrottleOffenders();
        int numberOfOffenders = rvStorageObj.getThrottleOffenderTypes().size();
        for (int i = 0; i < numberOfOffenders; i += 1) {
          datastoreRec.addThrottleOffender
          (rvStorageObj.getThrottleOffenderTypes().get(i),
          rvStorageObj.getThrottleOffenderIPs().get(i),
          rvStorageObj.getThrottleOffenderReferers().get(i),
          rvStorageObj.getThrottleOffenderCounts().get(i));
        }

        datastoreRec.setThrottled(false);
        OfyService.ofy().save().entity(datastoreRec).now();
      }});
    }
  }
}
