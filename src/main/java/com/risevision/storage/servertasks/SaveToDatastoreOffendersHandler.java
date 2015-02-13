package com.risevision.storage.servertasks;

import java.util.*;
import com.google.api.services.bigquery.model.*;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.entities.RvStorageObject;
import com.risevision.storage.datastore.OfyService;
import com.googlecode.objectify.VoidWork;

class SaveToDatastoreOffendersHandler implements ThrottleOffendersHandler {
  RvStorageObject rvStorageObj;

  public void handle(RvStorageObjectRowProcessor rp) {
    insertRvStorageObjectsIntoDatastore(rp);
  }

  private void insertRvStorageObjectsIntoDatastore(RvStorageObjectRowProcessor rp) {
    for (RvStorageObject rvStorageObject : rp.getRvStorageObjects()) {
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

        if (!datastoreRec.isThrottled()) {
          datastoreRec.setNeedsThrottling();
        }
        OfyService.ofy().save().entity(datastoreRec).now();
      }});
    }
  }
}
