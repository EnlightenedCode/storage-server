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
import java.util.logging.Level;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.ServiceException;
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
import com.risevision.storage.api.accessors.FileTagEntryAccessor;
import com.risevision.storage.api.accessors.TagDefinitionAccessor;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.api.impl.SubscriptionStatusFetcherImpl;
import com.risevision.storage.api.responses.GCSFilesResponse;
import com.risevision.storage.api.responses.ItemResponse;
import com.risevision.storage.api.responses.ListResponse;
import com.risevision.storage.api.responses.SimpleResponse;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.StorageEntity;
import com.risevision.storage.entities.SubscriptionStatus;
import com.risevision.storage.entities.TagDefinition;
import com.risevision.storage.gcs.GCSClient;
import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.tasks.BQUtils;


@Api(
name = "storage",
version = "v0.01",
clientIds = {com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID, Globals.STORE_CLIENT_ID}
)

public class StorageAPI extends AbstractAPI {
  private static final String bandwidthQryBegin = 
          "select bytes_this_month from " + Globals.DATASET_ID +
          ".BucketBandwidthMonthly where bucket = '";
  
  private SubscriptionStatusFetcher subscriptionStatusFetcher;
  private TagDefinitionAccessor tagDefinitionAccessor;
  private FileTagEntryAccessor fileTagEntryAccessor;

  private static final MemcacheService syncCache = 
       MemcacheServiceFactory.getMemcacheService("month-bucket-bandwidth");

  private static final StorageService gcsService =
  new StorageService(GCSClient.getStorageClient());

  {
    syncCache.setErrorHandler(ErrorHandlers
                             .getConsistentLogAndContinue(WARNING));
  }
  
  public StorageAPI() {
    this.subscriptionStatusFetcher = new SubscriptionStatusFetcherImpl();
    this.fileTagEntryAccessor = new FileTagEntryAccessor();
    this.tagDefinitionAccessor = new TagDefinitionAccessor();
  }

  private boolean hasNull(List<?> parameters) {
    for (Object param : parameters) {
      if (param == null) {
        return true;
      }
    }
    return false;
  }
  
  protected void verifyActiveSubscription(String companyId) throws ServiceFailedException {
    if (Globals.devserver) {return;}
    if (Strings.isNullOrEmpty(companyId)) {
      throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
    }
    
    SubscriptionStatus status = subscriptionStatusFetcher.getSubscriptionStatus(companyId);
    
    if(!status.isActive()) {
      log.info("Subscription is not active");
      throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
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
  
  /**
   * Verifies if the company's bucket has been created. In case it isn't, a new bucket with the correct name is created
   * 
   * @param companyId The company id
   * @param user The current logged in user
   * @param errorPrefix The prefix to use for translation messages
   * 
   * @throws ServiceFailedException In case any of the validation fails, or a server error occurs
   */
  protected void verifyAndCreateBucket(String companyId, User user, String errorPrefix) throws ServiceFailedException {
    SubscriptionStatus status = null;
    String bucketName = Globals.COMPANY_BUCKET_PREFIX + companyId;
    
    status = subscriptionStatusFetcher.getSubscriptionStatus(companyId);
    
    if(!status.isActive() && !status.isTrialAvailable()) {
      throw new ServiceFailedException(ServiceFailedException.FORBIDDEN, errorPrefix + "-inactive-subscription");
    }
    
    if(status.isTrialAvailable()) {
      initiateTrial(companyId);
    }

    if (gcsService.bucketExists(bucketName)) {return;}
    
    gcsService.createBucket(bucketName);
  }

  @ApiMethod(
  name = "files.get",
  path = "files",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getFiles(@Named("companyId") String companyId,
                                 @Nullable @Named("folder") String folder) {
    GCSFilesResponse result;
    result = new GCSFilesResponse();

    try {
      List<StorageObject> items = gcsService.getBucketItems(
        Globals.COMPANY_BUCKET_PREFIX + companyId,
        folder, "/");

      result.result = true;
      result.code = ServiceFailedException.OK;
      result.files = items;
    } catch (ServiceFailedException e) {
      if(e.getReason() == ServiceFailedException.NOT_FOUND) {
        result.result = true;
        result.code = ServiceFailedException.OK;
        result.files = new ArrayList<StorageObject>();
      }
      else {
        result.result = false;
        result.code = e.getReason();
        result.message = "Could not retrieve Bucket Items";
        log.warning("Could not retrieve Bucket Items - Status: " + e.getReason());
      }
    }

    return result;
  }

  @ApiMethod(
  name = "enablePublicRead",
  path = "enablePublicRead",
  httpMethod = HttpMethod.POST)
  public SimpleResponse enablePublicRead
  (@Named("companyId") String companyId, User user) {
    SimpleResponse result;
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      verifyActiveSubscription(companyId);
    } catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "User not permitted");
    }

    try {
      gcsService.enablePublicRead(companyId);

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Failed to ensure public read for company bucket";
      log.warning("Failed to ensure public read for company bucket: " + e.getReason());
    }

    return result;
  }

  @ApiMethod(
  name = "files.delete",
  path = "files",
  httpMethod = HttpMethod.POST)
  public SimpleResponse deleteFiles(@Named("companyId") String companyId,
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
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "delete-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "delete-content-producer", user.getEmail());
    }
    
    try {
      gcsService.deleteMediaItems(Globals.COMPANY_BUCKET_PREFIX + companyId,
                                  files);

      log.info("Deletion complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      log.warning("File Deletion Failed - Status: " + e.getReason());
      
      return new SimpleResponse(false, e.getReason(), "failed-delete", user.getEmail());
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
    SimpleResponse result;
    
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    if (Strings.isNullOrEmpty(companyId) || Strings.isNullOrEmpty(folder)) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "folder-company-unspecified", user.getEmail());
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "folder-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "folder-content-producer", user.getEmail());
    }
    
    try {
      verifyAndCreateBucket(companyId, user, "folder");
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, e.getReason(), e.getMessage(), user.getEmail());
    }
    
    try {
      gcsService.createFolder(Globals.COMPANY_BUCKET_PREFIX + companyId, folder);
      log.info("Folder created for company " + companyId);

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      log.warning("Folder creation failed - Status: " + e.getReason());
      return new SimpleResponse(false, e.getReason(), "create-folder-failed", user.getEmail());
    }

    return result;
  }

  @ApiMethod(
  name = "createBucket",
  path = "bucket",
  httpMethod = HttpMethod.POST)
  public SimpleResponse createBucket(@Named("companyId") String companyId,
                                     User user) {
    SimpleResponse result;
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }

    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
      initiateTrial(companyId);

      String bucketName = Globals.COMPANY_BUCKET_PREFIX + companyId;

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
                           @Named("companyId") String companyId
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
  public SimpleResponse deleteBucket(@Named("companyId") String companyId,
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
                                              @Nullable @Named("origin") String origin,
                                              User user) {
    SimpleResponse result;
    
    try {
      result = new SimpleResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "upload-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "upload-content-producer", user.getEmail());
    }
    
    try {
      verifyAndCreateBucket(companyId, user, "upload");
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, e.getReason(), e.getMessage(), user.getEmail());
    }

    try {
      log.info("Requesting resumable upload for " + result.userEmail);
      result.message = gcsService.getResumableUploadURI(Globals.COMPANY_BUCKET_PREFIX +
                                                        companyId,
                                                        fileName,
                                                        fileType,
                                                        origin);
      result.result = true;
    } catch (ServiceFailedException e) {
      log.warning("Upload URI request failed - Status: " + e.getReason());
      return new SimpleResponse(false, e.getReason(), "upload-uri-request-failed", user.getEmail());
    }
    
    return result;
  }
  
  /**
   * Generates a Signed URL for the given object
   * 
   * @param companyId The company id
   * @param fileName The object to download
   * @param fileType The file type
   * @param user The currently logged in user
   * 
   * @return The signed url
   */
  @ApiMethod(
  name = "getSignedDownloadURI",
  path = "getDownloadURI",
  httpMethod = HttpMethod.POST)
  public SimpleResponse getSignedDownloadURI(@Named("companyId") String companyId,
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
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "signed-url-verify-company", user.getEmail());
    }
    
    try {
      log.info("Requesting signed download uri for " + result.userEmail);
      result.message = gcsService.getSignedDownloadURI(Globals.COMPANY_BUCKET_PREFIX +
                                                       companyId,
                                                       fileName,
                                                       fileType);
      result.result = true;
    } catch (ServiceFailedException e) {
      result.result = false;
      result.message = "signed-download-uri-request-failed";
      result.userEmail = user.getEmail();
      log.warning("Download URI request failed - Status: " + e.getReason());
    }
    return result;
  }

  @ApiMethod(
  name = "trash.move",
  path = "trash",
  httpMethod = HttpMethod.POST)
  public SimpleResponse moveToTrash(@Named("companyId") String companyId,
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
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "trash-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "trash-content-producer", user.getEmail());
    }

    try {
      gcsService.moveToTrash(Globals.COMPANY_BUCKET_PREFIX + companyId, files);

      log.info("Move to trash complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      log.warning("File Move To Trash Failed - Status: " + e.getReason());
      
      return new SimpleResponse(false, e.getReason(), "failed-trash", user.getEmail());
    }

    return result;
  }

  @ApiMethod(
  name = "trash.restore",
  path = "trash",
  httpMethod = HttpMethod.PUT)
  public SimpleResponse restoreFromTrash(@Named("companyId") String companyId,
                                         @Nullable @Named("files") List<String> files,
                                         User user) {
    GCSFilesResponse result;
    try {
      result = new GCSFilesResponse(user);
    } catch (IllegalArgumentException e) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      verifyActiveSubscription(companyId);
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "restore-inactive-subscription", user.getEmail());
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "restore-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "restore-content-producer", user.getEmail());
    }

    try {
      gcsService.restoreFromTrash(Globals.COMPANY_BUCKET_PREFIX + companyId, files);

      log.info("Restore from trash complete");

      result.result = true;
      result.code = ServiceFailedException.OK;
    } catch (ServiceFailedException e) {
      log.warning("File Restore From Trash Failed - Status: " + e.getReason());
      
      return new SimpleResponse(false, e.getReason(), "failed-restore", user.getEmail());
    }

    return result;
  }

  @ApiMethod(
  name = "tagdef.put",
  path = "tagdef",
  httpMethod = HttpMethod.PUT)
  public SimpleResponse putTagDefinition(@Named("companyId") String companyId,
                                         @Named("type") String type,
                                         @Named("name") String name,
                                         @Nullable @Named("values") List<String> values,
                                         User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
    }
    
    try {
      TagDefinition tagDefinition = tagDefinitionAccessor.put(companyId, type, name, values, user);
      
      return new ItemResponse<TagDefinition>(user.getEmail(), tagDefinition);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed putTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "tagdef.get",
  path = "tagdef",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getTagDefinition(@Named("id") String id,
                                         User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      TagDefinition tagDefinition = tagDefinitionAccessor.get(id);
      
      if(tagDefinition == null) {
        throw new ValidationException("Tag definition does not exist");
      }
      
      try {
        new UserCompanyVerifier().verifyUserCompany(tagDefinition.getCompanyId(), user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
      }
      
      try {
        new UserRoleVerifier().verifyContentProducer(user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
      }
      
      return new ItemResponse<TagDefinition>(user.getEmail(), tagDefinition);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed getTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "tagdef.delete",
  path = "tagdef",
  httpMethod = HttpMethod.DELETE)
  public SimpleResponse deleteTagDefinition(@Named("id") String id,
                                            User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      TagDefinition tagDefinition = tagDefinitionAccessor.get(id);
      
      if(tagDefinition == null) {
        throw new ValidationException("Tag definition does not exist");
      }
      
      try {
        new UserCompanyVerifier().verifyUserCompany(tagDefinition.getCompanyId(), user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
      }
      
      try {
        new UserRoleVerifier().verifyContentProducer(user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
      }
      
      tagDefinitionAccessor.delete(id);
      
      return new ItemResponse<TagDefinition>(user.getEmail(), tagDefinition);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "tagdef.list",
  path = "tagdeflist",
  httpMethod = HttpMethod.GET)
  public SimpleResponse listTagDefinitions(@Named("companyId") String companyId,
                                           @Nullable @Named("search") String search,
                                           @Nullable @Named("limit") Integer limit,
                                           @Nullable @Named("sort") String sort,
                                           @Nullable @Named("cursor") String cursor,
                                           User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
    }
    
    try {
      PagedResult<TagDefinition> pagedResult = tagDefinitionAccessor.list(companyId, search, limit, sort, cursor);
      
      return new ListResponse<TagDefinition>(user.getEmail(), pagedResult.getList(), pagedResult.getCursor());
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "filetag.put",
  path = "filetag",
  httpMethod = HttpMethod.PUT)
  public SimpleResponse putFileTagEntry(@Named("companyId") String companyId,
                                        @Named("objectId") String objectId,
                                        @Named("type") String type,
                                        @Named("name") String name,
                                        @Nullable @Named("values") List<String> values,
                                        User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
    }
    
    try {
      FileTagEntry fileTagEntry = fileTagEntryAccessor.put(companyId, objectId, type, name, values, user);
      
      return new ItemResponse<FileTagEntry>(user.getEmail(), fileTagEntry);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "filetag.get",
  path = "filetag",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getFileTagEntry(@Named("id") String id,
                                        User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      FileTagEntry fileTagEntry = fileTagEntryAccessor.get(id);
      
      if(fileTagEntry == null) {
        throw new ValidationException("File tag entry does not exist");
      }
      
      try {
        new UserCompanyVerifier().verifyUserCompany(fileTagEntry.getCompanyId(), user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
      }
      
      try {
        new UserRoleVerifier().verifyContentProducer(user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
      }
      
      return new ItemResponse<FileTagEntry>(user.getEmail(), fileTagEntry);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed getTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "filetag.delete",
  path = "filetag",
  httpMethod = HttpMethod.DELETE)
  public SimpleResponse deleteFileTagEntry(@Named("id") String id,
                                           User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      FileTagEntry fileTagEntry = fileTagEntryAccessor.get(id);
      
      if(fileTagEntry == null) {
        throw new ValidationException("File tag entry does not exist");
      }
     
      try {
        new UserCompanyVerifier().verifyUserCompany(fileTagEntry.getCompanyId(), user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
      }
      
      try {
        new UserRoleVerifier().verifyContentProducer(user.getEmail());
      }
      catch (ServiceFailedException e) {
        return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
      }
      
      fileTagEntryAccessor.delete(id);
      
      return new ItemResponse<FileTagEntry>(user.getEmail(), fileTagEntry);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "filetag.list",
  path = "filetaglist",
  httpMethod = HttpMethod.GET)
  public SimpleResponse listFileTagEntry(@Named("companyId") String companyId,
                                         @Nullable @Named("search") String search,
                                         @Nullable @Named("limit") Integer limit,
                                         @Nullable @Named("sort") String sort,
                                         @Nullable @Named("cursor") String cursor,
                                         User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
    }
    
    try {
      new UserRoleVerifier().verifyContentProducer(user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-content-producer", user.getEmail());
    }
    
    try {
      PagedResult<FileTagEntry> pagedResult = fileTagEntryAccessor.list(companyId, search, limit, sort, cursor);
      
      return new ListResponse<FileTagEntry>(user.getEmail(), pagedResult.getList(), pagedResult.getCursor());
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }

  @ApiMethod(
  name = "files.listbytags",
  path = "filesbytag",
  httpMethod = HttpMethod.GET)
  public SimpleResponse listFilesByTags(@Named("companyId") String companyId,
                                        @Named("tags") List<String> tags,
                                        @Nullable @Named("returnTags") Boolean returnTags,
                                        User user) throws ServiceException {
    if(user == null) {
      return new SimpleResponse(false, ServiceFailedException.AUTHENTICATION_FAILED, "No user");
    }
    
    try {
      new UserCompanyVerifier().verifyUserCompany(companyId, user.getEmail());
    }
    catch (ServiceFailedException e) {
      return new SimpleResponse(false, ServiceFailedException.FORBIDDEN, "tagging-verify-company", user.getEmail());
    }
    
    try {
      List<StorageEntity> files = fileTagEntryAccessor.listFilesByTags(companyId, tags, returnTags != null ? returnTags : false);
      
      return new GCSFilesResponse(user, true, ServiceFailedException.OK, files);
    } catch (ValidationException e) {
      return new SimpleResponse(false, ServiceFailedException.CONFLICT, e.getMessage());
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed deleteTagDefinition", e);
      return new SimpleResponse(false, ServiceFailedException.SERVER_ERROR, e.getMessage());
    }
  }
}
