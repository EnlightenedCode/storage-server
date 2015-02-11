package com.risevision.storage.servertasks;

import java.util.*;
import com.google.api.services.bigquery.model.*;
import com.risevision.storage.datastore.DatastoreService;

class SaveToDatastoreOffendersHandler implements ThrottleOffendersHandler {
  HashMap<String, RvStorageObject> rvStorageObjects = new HashMap<>();

  RvStorageObject rvStorageObject;
  public void handle(List<TableRow> offenders) {
    for (TableRow offender : offenders) {
      List<TableCell> cells = offender.getF();
      String bucket = (String)cells.get(2).getV();
      String file = (String)cells.get(3).getV();

      rvStorageObject = rvStorageObjects.get(bucket + "\u00f1" + file);
      if (rvStorageObject == null) {
        rvStorageObject = new RvStorageObject(bucket, file);
      }

      rvStorageObject.addFileThrottleOffender
      (cells.get(0).getV() + "|" + cells.get(1).getV() + "|" + cells.get(4).getV());
      rvStorageObjects.put(bucket + "\u00f1" + file, rvStorageObject);
    }

    for (RvStorageObject rvStorageObject : rvStorageObjects) {
      OfyService.ofy().transact(new VoidWork() {
        public void vrun() {
          RvStorageObject datastoreRec = 
          OfyService.ofy().load().type(RvStorageObject.class)
          .filter(rvStorageObject.getBucket())
          .filter(rvStorageObject.getFile()).first().now();

          if (datastoreRec == null) {
            datastoreRec = new RvStorageObject(bucket, file);
          }

          datastoreRec.addFileThrottleOffenders
          (rvStorageObject.getFileThrottleOffenders());
          datastoreRec.setFileThrottle(true);
          OfyService.ofy().save(datastoreRec).now();
        }
      });
    }
  }
}
