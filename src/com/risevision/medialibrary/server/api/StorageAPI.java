package com.risevision.medialibrary.server.api;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.users.User;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.medialibrary.server.MediaLibraryService;
import com.risevision.medialibrary.server.MediaLibraryServiceImpl;
import com.risevision.medialibrary.server.Utils;
import com.risevision.medialibrary.server.api.responses.FilesResponse;
import com.risevision.medialibrary.server.api.responses.SignedPolicyResponse;
import com.risevision.medialibrary.server.api.responses.SimpleResponse;
import com.risevision.medialibrary.server.info.MediaItemInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;

@Api(
	    name = "storage",
	    version = "v0.01",
	    clientIds = {com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID, com.risevision.medialibrary.server.info.Globals.STORE_CLIENT_ID}
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
				return result;
			}

			log.info("User: " + user.getEmail());

			try {
				
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
			@Nullable @Named("bucketName") String bucketName,
			User user) {

		SimpleResponse result = new SimpleResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

			try {
				
				MediaLibraryService service = new MediaLibraryServiceImpl();
				
				service.createBucket(bucketName);
				
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

			MediaLibraryService service = new MediaLibraryServiceImpl();
			
			String signedPolicy = service.getSignedPolicy(policyBase64, null);
			
			log.info("Files Deleted");

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
	
}
