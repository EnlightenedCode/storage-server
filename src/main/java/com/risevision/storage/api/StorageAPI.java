package com.risevision.storage.api;

import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.risevision.storage.Globals;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.Utils;
import com.risevision.storage.api.responses.GCSFilesResponse;
import com.risevision.storage.api.responses.SimpleResponse;
import com.risevision.storage.api.responses.StringResponse;
import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.tasks.BQUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


@Api(
name = "storage",
version = "v0.01",
clientIds = {com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID, Globals.STORE_CLIENT_ID}
)

public class StorageAPI extends AbstractAPI {
  private static final String bandwidthQryBegin = 
          "select bytes_this_month from " + Globals.DATASET_ID +
          ".BucketBandwidthMonthly where bucket = '";

  private static final MemcacheService syncCache = 
       MemcacheServiceFactory.getMemcacheService("month-bucket-bandwidth");

  {
    syncCache.setErrorHandler(ErrorHandlers
                             .getConsistentLogAndContinue(WARNING));
  }


  private boolean hasNull(List<?> parameters) {
    for (Object param : parameters) {
      if (param == null) {
        return true;
      }
    }
    return false;
  }

  private void verifySubscription(String companyId)
  throws ServiceFailedException {
    if (Globals.devserver) {return;}
    if (Strings.isNullOrEmpty(companyId)) {
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    }

    try {
      URL url = new URL(Globals.SUBSCRIPTION_STATUS_URL
                       .replace("companyId", companyId));
      java.net.HttpURLConnection httpConn = 
        (java.net.HttpURLConnection)url.openConnection();
      httpConn.setInstanceFollowRedirects(false);

      BufferedReader reader = new BufferedReader(
                              new InputStreamReader(httpConn.getInputStream()));

      String result = reader.readLine();
      log.info("Store product status result: " + result);
      reader.close();

      if (!result.contains("\"status\":\"Subscribed\"") &&
          !result.contains("\"status\":\"On Trial\"")) {
        throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
      }
    } catch (MalformedURLException e) {
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    } catch (IOException e) {
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }

  private void initiateTrial(String companyId)
  throws ServiceFailedException {
    if (Globals.devserver) {return;}
    if (Strings.isNullOrEmpty(companyId)) {
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    }

    try {
      //The store subscription api will create a trial if one is available
      URL url = new URL(Globals.SUBSCRIPTION_AUTH_URL + companyId);
      java.net.HttpURLConnection httpConn = 
        (java.net.HttpURLConnection)url.openConnection();
      httpConn.setInstanceFollowRedirects(false);

      BufferedReader reader = new BufferedReader(
                              new InputStreamReader(httpConn.getInputStream()));

      String result = reader.readLine();
      log.info("Store auth result: " + result);
      reader.close();
    } catch (MalformedURLException e) {
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    } catch (IOException e) {
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }

  @ApiMethod(
  name = "files.get",
  path = "files",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getFiles(@Nullable @Named("companyId") String companyId,
                                 @Nullable @Named("folder") String folder,
                                 User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      StorageService gcsService = StorageService.getInstance();
      List<StorageObject> items = gcsService.getBucketItems(
        Globals.COMPANY_BUCKET_PREFIX + companyId,
        folder, "/");

      result.result = true;
      result.code = ServiceFailedException.OK;
      result.files = items;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Could not retrieve Bucket Items";
      log.warning("Could not retrieve Bucket Items - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "files.delete",
  path = "files",
  httpMethod = HttpMethod.POST)
  public SimpleResponse deleteFiles(@Nullable @Named("companyId") String companyId,
                                    @Nullable @Named("files") List<String> files,
                                    User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());

      StorageService gcsService = StorageService.getInstance();
      gcsService.deleteMediaItems(Globals.COMPANY_BUCKET_PREFIX + companyId,
                                  files);

      log.info("Deletion complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "File Deletion Failed";
      log.warning("File Deletion Failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "createFolder",
  path = "folder",
  httpMethod = HttpMethod.POST)
  public SimpleResponse createFolder(@Named("companyId") String companyId,
                                     @Named("folder") String folder,
                                     User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    if (Strings.isNullOrEmpty(companyId) ||
        Strings.isNullOrEmpty(folder)) {
      result.message = "Unspecified folder or company";
      result.result = false;
      return result;
    }

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      StorageService gcsService = StorageService.getInstance();
      gcsService.createFolder(Globals.COMPANY_BUCKET_PREFIX + companyId
                             ,folder);
      log.info("Folder created for company " + companyId);

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Folder creation failed";
      log.warning("Folder creation failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "createBucket",
  path = "bucket",
  httpMethod = HttpMethod.POST)
  public SimpleResponse createBucket(@Nullable @Named("companyId") String companyId,
                                     User user) {
    SimpleResponse result;
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    String bucketName; 

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      initiateTrial(companyId);

      bucketName = Globals.COMPANY_BUCKET_PREFIX + companyId;

      StorageService gcsService = StorageService.getInstance();
      gcsService.createBucket(bucketName);

      result.result = true;
      result.code = ServiceFailedException.OK;

    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Bucket Creation Failed";

      log.warning("Bucket Creation Failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "getBucketBandwidth",
  path = "bucketBandwidth",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getBucketBandwidth(
                           @Nullable @Named("companyId") String companyId
                          ,User user) {
    SimpleResponse result;

    try {
        result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
        return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    if (hasNull(new ArrayList<Object>(asList(companyId, user)))) {
      result.message = "unexpected-null-parameter";
      result.result = false;
      return result;
    }

    log.info("User: " + user.getEmail());

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      String bandwidth = (String)syncCache.get(companyId);
      if (bandwidth == null) {
        log.info("Cache miss - Fetching value from bigquery.");
        String bandwidthQry = bandwidthQryBegin + companyId + "'";
        bandwidth = (String)BQUtils.getSingleValueFromQuery(bandwidthQry);
        syncCache.put(companyId, bandwidth);
      }

      result.message = bandwidth;
      result.result = true;
      result.code = ServiceFailedException.OK;

    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "bandwidth-query-failed";

      log.warning("Bucket bandwidth query failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "deleteBucket",
  path = "bucket",
  httpMethod = HttpMethod.DELETE)
  public SimpleResponse deleteBucket(@Nullable @Named("companyId") String companyId,
                                     User user) {
    SimpleResponse result;
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    String bucketName; 

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      bucketName = Globals.COMPANY_BUCKET_PREFIX + companyId;

      StorageService gcsService = StorageService.getInstance();
      gcsService.deleteBucket(bucketName);

      result.result = true;
      result.code = ServiceFailedException.OK;

    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Bucket deletion failed";
      log.warning("Bucket Deletion Failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "getResumableUploadURI",
  path = "getUploadURI",
  httpMethod = HttpMethod.POST)
  public SimpleResponse getResumableUploadURI(@Named("companyId") String companyId,
                                              @Named("fileName") String fileName,
                                              @Nullable @Named("fileType") String fileType,
                                              User user) {
    SimpleResponse result;
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      StorageService gcsService = StorageService.getInstance();
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      verifySubscription(companyId);
      log.info("Requesting resumable upload for " + result.userEmail);
      result.message = gcsService.getResumableUploadURI(Globals.COMPANY_BUCKET_PREFIX +
                                                        companyId,
                                                        fileName,
                                                        fileType);
      result.result = true;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.message = "upload-uri-request-failed";
      log.warning("Upload URI request failed - Status: " + e.getReason());
    }
    return result;
  }

  @ApiMethod(
  name = "trash.move",
  path = "trash",
  httpMethod = HttpMethod.POST)
  public SimpleResponse moveToTrash(@Nullable @Named("companyId") String companyId,
                                    @Nullable @Named("files") List<String> files,
                                    User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      verifyUserCompany(companyId, user.getEmail());

      StorageService gcsService = StorageService.getInstance();
      gcsService.moveToTrash(Globals.COMPANY_BUCKET_PREFIX + companyId, files);

      log.info("Move to trash complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "File move to trash Failed";
      log.warning("File Move To Trash Failed - Status: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "trash.restore",
  path = "trash",
  httpMethod = HttpMethod.PUT)
  public SimpleResponse restoreFromTrash(@Nullable @Named("companyId") String companyId,
                                         @Nullable @Named("files") List<String> files,
                                         User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      verifyUserCompany(companyId, user.getEmail());

      StorageService gcsService = StorageService.getInstance();
      gcsService.restoreFromTrash(Globals.COMPANY_BUCKET_PREFIX + companyId, files);

      log.info("Restore from trash complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "File restore from trash Failed";
      log.warning("File Restore From Trash Failed - Status: " + e.getReason());
    }

    return result;
  }
}
