package com.risevision.storage.api;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.users.User;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.MediaLibraryServiceImpl;
import com.risevision.storage.Utils;
import com.risevision.storage.api.responses.FilesResponse;
import com.risevision.storage.api.responses.SignedPolicyResponse;
import com.risevision.storage.api.responses.SimpleResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.security.AccessResource;

@Api(
	    name = "storage",
	    version = "v0.01",
	    clientIds = {com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID, com.risevision.storage.info.Globals.STORE_CLIENT_ID}
)

public class StorageAPI extends AbstractAPI {
	
	@ApiMethod(
			name = "files.get",
			path = "files",
			httpMethod = HttpMethod.GET
	)
	public SimpleResponse getFiles(
			@Nullable @Named("companyId") String companyId,
			User user) {

		FilesResponse result = new FilesResponse();

		try {

			if (user == null) {
				result.message = "No user";
				result.code = ServiceFailedException.AUTHENTICATION_FAILED;
				return result;
			}

			log.info("User: " + user.getEmail());

			try {
				AccessResource resource = new AccessResource(companyId, user.getEmail());
				resource.verify();
				
				MediaLibraryService service = new MediaLibraryServiceImpl();
				
				List<MediaItemInfo> items =  service.getBucketItems(getBucketName(companyId));
				
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

		FilesResponse result = new FilesResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

				try {
					AccessResource resource = new AccessResource(companyId, user.getEmail());
					resource.verify();
					
					MediaLibraryService service = new MediaLibraryServiceImpl();
					
					service.deleteMediaItems(getBucketName(companyId), files);
					
					log.info("Files Deleted");
					
					List<MediaItemInfo> items =  service.getBucketItems(getBucketName(companyId));
					
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
			name = "createBucket",
			path = "bucket",
			httpMethod = HttpMethod.POST
	)
	public SimpleResponse createBucket(
			@Nullable @Named("companyId") String companyId,
			User user) {

		SimpleResponse result = new SimpleResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

			try {
				AccessResource resource = new AccessResource(companyId, user.getEmail());
				resource.verify();
				
				MediaLibraryService service = new MediaLibraryServiceImpl();
				
				service.createBucket(getBucketName(companyId));
				
				log.info("Bucket Created");

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

		SignedPolicyResponse result = new SignedPolicyResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

			try {

				AccessResource resource = new AccessResource(companyId, user.getEmail());
				resource.verify();
				
			} catch (ServiceFailedException e) {

				result.result = false;
				result.code = e.getReason();
				result.message = "Authentication Failed";
				
				log.warning("Authentication Failed - Status: " + e.getReason());
				
			}
			
			MediaLibraryService service = new MediaLibraryServiceImpl();
			
			String signedPolicy = service.getSignedPolicy(policyBase64, null);
			
			log.info("Policy Signed");

			result.result = true;
			result.code = ServiceFailedException.OK;
			result.signedPolicy = signedPolicy;

		} catch (Exception e) {
			Utils.logException(e);
			
			result.result = false;
			result.code = ServiceFailedException.SERVER_ERROR;
			result.message = "Internal Error.";
		}

		return result;

	}
	
	private String getBucketName(String companyId) throws ServiceFailedException {
		if (RiseUtils.strIsNullOrEmpty(companyId)) {
			throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
		}
		
		return "risemedialibrary-" + companyId;
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
