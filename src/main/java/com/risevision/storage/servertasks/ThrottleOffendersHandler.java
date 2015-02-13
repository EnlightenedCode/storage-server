package com.risevision.storage.servertasks;
import com.google.api.services.bigquery.model.TableRow;
import java.util.List;

interface ThrottleOffendersHandler {
  void handle(RvStorageObjectRowProcessor rp);
}
