package com.risevision.storage.gcs.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.bigquery.model.TableRow;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.ActiveBucketFetcher;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.tasks.BQUtils;

public class ActiveBucketFetcherImpl implements ActiveBucketFetcher {
  @Override
  public List<String> execute(int days) throws ServiceFailedException {
    Long fromTS = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000;
    String query = "select substr(cs_bucket,18) as companyid from " + Globals.DATASET_ID + ".UsageLogs where cs_bucket is not null and time_timestamp > " + fromTS * 1000 + " group by companyid";
    List<TableRow> result = BQUtils.executeQuery(query);
    ArrayList<String> companies = new ArrayList<String>();
    
    for(TableRow row : result) {
      companies.add((String) row.getF().get(0).getV());
    }
    
    return companies;
  }
}
