package com.risevision.storage.api;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

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
			httpMethod = HttpMethod.GET
	)
	public SimpleResponse getFiles(
			@Nullable @Named("companyId") String companyId,
			@Nullable @Named("folder") String folder,
			User user) {

		GCSFilesResponse result = new GCSFilesResponse();

		try {

			if (user == null) {
				result.message = "No user";
				result.code = ServiceFailedException.AUTHENTICATION_FAILED;
				return result;
			}

			log.info("User: " + user.getEmail());

			try {
                                verifyUserCompany(companyId, user.getEmail());
				MediaLibraryService gcsService = MediaLibraryService.getGCSInstance();
                                List<StorageObject> items;
				
                                items =  gcsService.getBucketItems(MediaLibraryService.getBucketName(companyId), folder, "/");

				result.result = true;
				result.code = ServiceFailedException.OK;
				result.files = items;
				
			} catch (ServiceFailedException e) {

				result.result = false;
				result.code = e.getReason();
				result.message = "Could not retrieve Bucket Items";
				
				log.warning("Could not retrieve Bucket Items - Status: " + e.getReason());
				
			}

		} catch (Exception e) {
			Utils.logException(e);
			
			result.result = false;
			result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	@ApiMethod(
			name = "files.delete",
			path = "files",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse deleteFiles(
			@Nullable @Named("companyId") String companyId,
			@Nullable @Named("files") List<String> files,
			User user) {

		GCSFilesResponse result = new GCSFilesResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

				try {
                                        verifyUserCompany(companyId, user.getEmail());
					
					MediaLibraryService service = MediaLibraryService.getInstance();
					MediaLibraryService gcsService = MediaLibraryService.getGCSInstance();
					
					service.deleteMediaItems(MediaLibraryService.getBucketName(companyId), files);
					
					log.info("Files Deleted");
					
					List<StorageObject> items =  gcsService.getBucketItems(MediaLibraryService.getBucketName(companyId));
					
					result.result = true;
					result.code = ServiceFailedException.OK;
					
					result.files = items;
					
				} catch (ServiceFailedException e) {

					result.result = false;
					result.code = e.getReason();
					result.message = "File Deletion Failed";
					
					log.warning("File Deletion Failed - Status: " + e.getReason());
					
				}


		} catch (Exception e) {
			Utils.logException(e);
			
			result.result = false;
			result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	@ApiMethod(
			name = "createFolder",
			path = "folder",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse createFolder(
			@Nullable @Named("companyId") String companyId,
			@Nullable @Named("folder") String folder,
                        User user) {

		GCSFilesResponse result = new GCSFilesResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}
			if (folder == null) {
				result.message = "No folder specified";
				return result;
			}

			log.info("User: " + user.getEmail());

				try {
                                        verifyUserCompany(companyId, user.getEmail());
					
					MediaLibraryService gcsService = MediaLibraryService.getGCSInstance();
					
					gcsService.createFolder(MediaLibraryService.getBucketName(companyId), folder);
					
					log.info("Folder created");
					
					List<StorageObject> items =  gcsService.getBucketItems(MediaLibraryService.getBucketName(companyId));
					
					result.result = true;
					result.code = ServiceFailedException.OK;
					
					result.files = items;
					
				} catch (ServiceFailedException e) {

					result.result = false;
					result.code = e.getReason();
					result.message = "Folder creation failed";
					
					log.warning("Folder creation failed - Status: " + e.getReason());
					
				}


		} catch (Exception e) {
			Utils.logException(e);
			
			result.result = false;
			result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	
	@ApiMethod(
			name = "file.url",
			path = "file",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse getFileUrl(
			@Nullable @Named("companyId") String companyId,
			@Nullable @Named("file") String file,
			User user) {

		StringResponse result = new StringResponse();

		try {

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
				
			}
			
			MediaLibraryService service = MediaLibraryService.getInstance();

			String fileUrl = service.getMediaItemUrl(MediaLibraryService.getBucketName(companyId), file);
			
			log.warning(fileUrl);
			
			result.result = true;
			result.code = ServiceFailedException.OK;
			result.response = fileUrl;

		} catch (Exception e) {
			Utils.logException(e);
			
			result.result = false;
			result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	@ApiMethod(
			name = "createBucket",
			path = "bucket",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse createBucket(
			@Nullable @Named("companyId") String companyId,
			User user) {
SimpleResponse result = new SimpleResponse(); try { if (user == null) {
				result.message = "No user";
				return result;
			}

                        String bucketName; 

			log.info("User: " + user.getEmail());

			try {
                              verifyUserCompany(companyId, user.getEmail());
			      MediaLibraryService service = MediaLibraryService.getInstance();
                              bucketName = MediaLibraryService.getBucketName(companyId);

                              MediaLibraryService gcsService = MediaLibraryService.getGCSInstance();
                              gcsService.createBucket(bucketName);
                              
                              log.info("Bucket Created");


/*
                              service.updateBucketProperty(bucketName, "logging", Globals.LOGGING_ENABLED_XML.replace("%bucketName%", bucketName).replace("%logBucket%", Globals.LOGS_BUCKET_NAME));

                              log.info("Bucket Logging Enabled");
                              */

                              result.result = true;
                              result.code = ServiceFailedException.OK;
                              
                      } catch (ServiceFailedException e) {

                              result.result = false;
                              result.code = e.getReason();
                              result.message = "Bucket Creation Failed";
                              
                              log.warning("Bucket Creation Failed - Status: " + e.getReason());
                              
                      }

              } catch (Exception e) {
                      Utils.logException(e);
                      
                      result.result = false;
                      result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	@ApiMethod(
			name = "signPolicy",
			path = "policy",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse signPolicy(
			@Nullable @Named("companyId") String companyId,
			@Nullable @Named("policyBase64") String policyBase64,
			User user) {

		StringResponse result = new StringResponse();

		try {

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
				
			}
			
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
	
//	@ApiMethod(
//			name = "enable",
//			path = "company",
//			httpMethod = HttpMethod.POST
//	)
//	public SimpleResponse enableMediaLibrary(
//			@Nullable @Named("companyId") String companyId,
//			User user) {
//
//		SimpleResponse result = new SimpleResponse();
//
//		try {
//
//			if (user == null) {
//				result.message = "No user";
//				return result;
//			}
//
//			log.info("User: " + user.getEmail());
//
//			try {
//				// Authorization check is performed when calling the API
////				AuthenticationService.checkAuthorization(companyId, user.getEmail());
//
//				CompanyInfo company = CacheUtils.getUserCompany(companyId, user.getEmail());
//				
//				company.enableMediaLibrary();
//				
//				CompanyService.getInstance().saveCompany(company, user.getEmail());
//				
//				CacheUtils.updateCompany(company, user.getEmail());
//				
//				log.info("Enabled the Media Library for the Company");
//				
//				result.result = true;
//				result.code = ServiceFailedException.OK;
//				
//			} catch (ServiceFailedException e) {
//
//				result.result = false;
//				result.code = e.getReason();
//				result.message = "Bucket Creation Failed";
//				
//				log.warning("Enabling the Media Library Failed - Status: " + e.getReason());
//				
//			}
//
//		} catch (Exception e) {
//			Utils.logException(e);
//			
//			result.result = false;
//			result.code = ServiceFailedException.SERVER_ERROR;
//			result.message = "Internal Error.";
//		}
//
//		return result;
//
//	}
	
}
