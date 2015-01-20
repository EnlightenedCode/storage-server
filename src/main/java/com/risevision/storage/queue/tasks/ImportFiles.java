package com.risevision.storage.queue.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.services.bigquery.model.*;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.base.Strings;
import com.google.common.base.Joiner;
import com.risevision.storage.Globals;
import com.risevision.storage.gcs.*;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.queue.QueueServlet;

public class ImportFiles extends AbstractTask {

  private static final String LOGS_BUCKET_URL = "gs://" + Globals.LOGS_BUCKET_NAME + "/";

  private static final int JOB_USAGE = 1;
  private static final int JOB_STORAGE = 0;

  private static final String USAGE_TABLE = "UsageLogs";
  private static final String USAGE_TABLE_TEMP = "UsageLogsTemp";
  private static final String STORAGE_TABLE = "StorageLogs";
  private static final String STORAGE_TABLE_TEMP = "StorageLogsTemp";
  private static final StorageService gcsService =
  new StorageService(GCSClient.getStorageClient());

  public static void runJob() throws Exception {
    runJob(JOB_STORAGE);

    QueueServlet.enqueueJob(Integer.toString(JOB_USAGE));
  }

  public static String runJob(int jobType) throws Exception {
    List<String> sources = new ArrayList<String>();
    List<String> files = new ArrayList<String>();


    String lastItem = null;
    String logDate = "";

    List<StorageObject> items = gcsService.getBucketItems(Globals.LOGS_BUCKET_NAME, null, null);

    for (StorageObject item: items) {
      if (item.getName().contains("storage") && jobType == JOB_STORAGE) {
        if (Strings.isNullOrEmpty(logDate)) {
          logDate = getStorageLogDate(item.getName());
        }

        if (item.getName().contains(logDate)) {
          sources.add(LOGS_BUCKET_URL + item.getName());
          files.add(item.getName());
        }
      }
      else if (item.getName().contains("usage") && jobType == JOB_USAGE) {
        sources.add(LOGS_BUCKET_URL + item.getName());
        files.add(item.getName());
      }

      if (sources.size() >= 200) {
        break;
      }
    }

    if (sources.size() == 0) {
      log.info("Done");

      return "Done";
    }

    String jobId;
    if (jobType == JOB_STORAGE) {
      jobId = runStorageJob(sources);
    }
    else {
      jobId =  runUsageJob(sources);
    }

    String filesString = Joiner.on(",").join(files);

    QueueServlet.enqueueCheckImportJob(jobId, Integer.toString(jobType), filesString);

    log.info("Files (" + files.size() + ") first: " + files.get(0) + " last: " + files.get(files.size() - 1));
    return "Queued";
  }

  public static void postProcess(String filesString, int jobType) throws Exception {
    // delete the files
    List<String> files = new ArrayList<String>(Arrays.asList(filesString.split(",")));

    if (files.size() > 0) {
      StorageService gcsService = new StorageService(GCSClient.getStorageClient());

      log.info("Removing Files (" + files.size() + ") first: " + files.get(0) + " last: " + files.get(files.size() - 1));

      List<String> failedFiles = gcsService.deleteMediaItems(Globals.LOGS_BUCKET_NAME, files);

      if (failedFiles.size() > 0) {
        filesString = Joiner.on(",").join(failedFiles);

        QueueServlet.enqueueCheckImportJob("", Integer.toString(jobType), filesString);

        log.info("Requeued delete job for: " + filesString);

        return;
      }
    }

    if (jobType == ImportFiles.JOB_STORAGE) {
      runStorageMoveJob(files.get(0));
    }
    else {
      runUsageMoveJob();
    }

  }

  public static void runStorageMoveJob(String filename) throws Exception {
    String dateToken = getStorageLogDate(filename);
    DateFormat inputDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    Date date = inputDateFormat.parse(dateToken);

    String query =
      "SELECT bucket, storage_byte_hours, SEC_TO_TIMESTAMP(" + date.getTime() / 1000 + ") as date "
      + "FROM [" + Globals.DATASET_ID + "." + STORAGE_TABLE_TEMP + "];";

    String jobId = BQUtils.startQuery(query, STORAGE_TABLE, BQUtils.WRITE_DISPOSITION_APPEND);
    QueueServlet.enqueueCheckMoveJob(jobId, Integer.toString(JOB_STORAGE));
  }

  public static void runUsageMoveJob() throws Exception {

    String query =
      "SELECT USEC_TO_TIMESTAMP(INTEGER(time_micros)) as time_timestamp, * "
      + "FROM [" + Globals.DATASET_ID + "." + USAGE_TABLE_TEMP + "];";
    String jobId = BQUtils.startQuery(query, USAGE_TABLE, BQUtils.WRITE_DISPOSITION_APPEND);

    QueueServlet.enqueueCheckMoveJob(jobId, Integer.toString(JOB_USAGE));
  }

  private static String getStorageLogDate(String filename) {
    // Parse Date part of the Storage log filename: [bucket]_storage_2014_02_09_08_00_00_0554a_v0
    String logDate = filename.substring(filename.indexOf("_storage_") + "_storage_".length());
    logDate = logDate.substring(0, 10);

    return logDate;
  }

  private static String runStorageJob(List<String> sources) throws Exception {
    TableSchema schema = getStorageSchema();
    String jobId =  BQUtils.runFilesJob(STORAGE_TABLE_TEMP, schema, sources);
    return jobId;
  }

  private static String runUsageJob(List<String> sources) throws Exception {
    TableSchema schema = getUsageSchema();
    String jobId = BQUtils.runFilesJob(USAGE_TABLE_TEMP, schema, sources);
    return jobId;
  }

  private static TableSchema getStorageSchema() {
    TableSchema schema = new TableSchema();

    List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();

    fields.add(getTableField("bucket", "string"));
    fields.add(getTableField("storage_byte_hours", "string"));

    schema.setFields(fields);

    return schema;
  }

  private static TableSchema getUsageSchema() {
    TableSchema schema = new TableSchema();

    List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();

    fields.add(getTableField("time_micros", "string"));
    fields.add(getTableField("c_ip", "string"));
    fields.add(getTableField("c_ip_type", "integer"));
    fields.add(getTableField("c_ip_region", "string"));
    fields.add(getTableField("cs_method", "string"));
    fields.add(getTableField("cs_uri", "string"));
    fields.add(getTableField("sc_status", "integer"));
    fields.add(getTableField("cs_bytes", "integer"));
    fields.add(getTableField("sc_bytes", "integer"));
    fields.add(getTableField("time_taken_micros", "integer"));
    fields.add(getTableField("cs_host", "string"));
    fields.add(getTableField("cs_referer", "string"));
    fields.add(getTableField("cs_user_agent", "string"));
    fields.add(getTableField("s_request_id", "string"));
    fields.add(getTableField("cs_operation", "string"));
    fields.add(getTableField("cs_bucket", "string"));
    fields.add(getTableField("cs_object", "string"));

    schema.setFields(fields);

    return schema;
  }

  private static TableFieldSchema getTableField(String name, String type) {
    TableFieldSchema tableField = new TableFieldSchema();
    tableField.setName(name);
    tableField.setType(type);

    return tableField;
  }
}
