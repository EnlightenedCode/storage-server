package com.risevision.storage.api;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import com.google.common.base.Strings;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.users.User;
import com.risevision.storage.Globals;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.Utils;
import com.risevision.storage.api.responses.FilesResponse;
import com.risevision.storage.api.responses.GCSFilesResponse;
import com.risevision.storage.api.responses.StringResponse;
import com.risevision.storage.api.responses.SimpleResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.gcs.StorageService;
import com.google.api.services.storage.model.StorageObject;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.security.AccessResource;

@Api(
name = "storage",
version = "v0.01",
clientIds = {com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID, Globals.STORE_CLIENT_ID}
)

public class StorageAPI extends AbstractAPI {

  private void verifyUserCompany(String companyId, String email)
    throws ServiceFailedException {
    if (Globals.devserver) {return;}
    AccessResource resource = new AccessResource(companyId, email);
    resource.verify();
  }

  @ApiMethod(
  name = "files.get",
  path = "files",
  httpMethod = HttpMethod.GET)
  public SimpleResponse getFiles(@Nullable @Named("companyId") String companyId,
                                 @Nullable @Named("folder") String folder,
                                 User user) {

    GCSFilesResponse result = new GCSFilesResponse();
    if (user == null) {
      result.message = "No user";
      result.code = ServiceFailedException.AUTHENTICATION_FAILED;
      return result;
    }

    log.info("User: " + user.getEmail());
    try {
      verifyUserCompany(companyId, user.getEmail());
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
    GCSFilesResponse result = new GCSFilesResponse();
    if (user == null) {
      result.message = "No user";
      return result;
    }
    log.info("User: " + user.getEmail());
    try {
      verifyUserCompany(companyId, user.getEmail());

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
    GCSFilesResponse result = new GCSFilesResponse();
    if (user == null) {
      result.message = "No user";
      return result;
    }

    if (Strings.isNullOrEmpty(companyId) ||
        Strings.isNullOrEmpty(folder)) {
      result.message = "Unspecified folder or company";
      return result;
    }

    log.info("User: " + user.getEmail());

    try {
      verifyUserCompany(companyId, user.getEmail());
      StorageService gcsService = StorageService.getInstance();
      gcsService.createFolder(Globals.COMPANY_BUCKET_PREFIX + companyId, folder);
      gcsService.setBucketPublicRead(Globals.COMPANY_BUCKET_PREFIX + companyId);
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
    SimpleResponse result = new SimpleResponse();
    if (user == null) {
      result.message = "No user";
      return result;
    }

    String bucketName; 
    log.info("User: " + user.getEmail());

    try {
      verifyUserCompany(companyId, user.getEmail());
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
  name = "deleteBucket",
  path = "bucket",
  httpMethod = HttpMethod.DELETE)
  public SimpleResponse deleteBucket(@Nullable @Named("companyId") String companyId,
                                     User user) {
    SimpleResponse result = new SimpleResponse();
    String bucketName; 
    if (user == null) {
      result.message = "No user";
      return result;
    }

    log.info("User: " + user.getEmail());
    try {
      verifyUserCompany(companyId, user.getEmail());
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
  name = "signPolicy",
  path = "policy",
  httpMethod = HttpMethod.POST)
  public SimpleResponse signPolicy(@Nullable @Named("companyId") String companyId,
                                   @Nullable @Named("policyBase64") String policyBase64,
                                   User user) {
    StringResponse result = new StringResponse();
    if (user == null) {
      result.message = "No user";
      return result;
    }
    log.info("User: " + user.getEmail());

    try {
      verifyUserCompany(companyId, user.getEmail());
    } catch (ServiceFailedException e) {
      result.result = false;
      result.code = e.getReason();
      result.message = "Authentication Failed";
      log.warning("Authentication Failed - Status: " + e.getReason());
      return result;
    }

    try {
      MediaLibraryService service = MediaLibraryService.getInstance();
      String signedPolicy = service.getSignedPolicy(policyBase64);
      log.info("Policy Signed");

      result.result = true;
      result.code = ServiceFailedException.OK;
      result.response = signedPolicy;
    } catch (Exception e) {
      Utils.logException(e);
      result.result = false;
      result.code = ServiceFailedException.SERVER_ERROR;
      result.message = "Internal Error.";
    }

    return result;
  }
}
