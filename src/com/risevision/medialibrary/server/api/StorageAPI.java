package com.risevision.medialibrary.server.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.users.User;
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
	
//	@ApiMethod(
//			name = "company.get",
//			path = "company",
//			httpMethod = HttpMethod.GET
//	)
//	public ItemResponse get(
//			@Named("id") String id,
//			User user) {
//		
//		ItemResponse result = new ItemResponse();
//
//		if (user == null) {
//			result.message = "No user";
//			return result;
//		}
//
//		log.info("User: " + user.getUserId());
//		
//		com.risevision.directory.documents.Company c = com.risevision.directory.documents.Company.get(id);
//		
//		result.result = c != null;
//		result.code = c != null ? 0 : -1;
//		
//		result.item = c != null ? new Company(c) : null;
//		
//		return result;
//		
//	}
	
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

//				try {
					
					MediaLibraryService service = new MediaLibraryServiceImpl();
					
					ArrayList<MediaItemInfo> items =  service.getBucketItems("risemedialibrary-" + companyId);
					
					log.info("Files Deleted");

					result.result = true;
					result.code = ServiceFailedException.OK;
					result.files = items;
					
//				} catch (ServiceFailedException e) {
//
//					result.result = false;
//					result.code = e.getReason();
//					result.message = "Bucket Creation Failed";
//					
//					log.warning("Bucket Creation Failed - Status: " + e.getReason());
//					
//				}


		} catch (Exception e) {
//			Utils.logException(e);
			result.result = false;
			result.code = -1;
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
//			@Nullable @Named("bucketName") String bucketName,
			@Nullable @Named("files") ArrayList<String> files,
			User user) {

		SimpleResponse result = new SimpleResponse();

		try {

			if (user == null) {
				result.message = "No user";
				return result;
			}

			log.info("User: " + user.getEmail());

//				try {
					
					MediaLibraryService service = new MediaLibraryServiceImpl();
					
//					service.deleteMediaItems(bucketName, files);
					
					log.info("Files Deleted");

					result.result = true;
					result.code = ServiceFailedException.OK;
					
//				} catch (ServiceFailedException e) {
//
//					result.result = false;
//					result.code = e.getReason();
//					result.message = "Bucket Creation Failed";
//					
//					log.warning("Bucket Creation Failed - Status: " + e.getReason());
//					
//				}


		} catch (Exception e) {
//			Utils.logException(e);
			result.result = false;
			result.code = -1;
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

//				try {
					
					MediaLibraryService service = new MediaLibraryServiceImpl();
					
//					service.createBucket(bucketName);
					
					log.info("Bucket Created");

					result.result = true;
					result.code = ServiceFailedException.OK;
					
//				} catch (ServiceFailedException e) {
//
//					result.result = false;
//					result.code = e.getReason();
//					result.message = "Bucket Creation Failed";
//					
//					log.warning("Bucket Creation Failed - Status: " + e.getReason());
//					
//				}


		} catch (Exception e) {
//			Utils.logException(e);
			result.result = false;
			result.code = -1;
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

//				try {
					
					MediaLibraryService service = new MediaLibraryServiceImpl();
					
					String signedPolicy = service.getSignedPolicy(policyBase64, null);
					
					log.info("Files Deleted");

					result.result = true;
					result.code = ServiceFailedException.OK;
					result.signedPolicy = signedPolicy;
					
//				} catch (ServiceFailedException e) {
//
//					result.result = false;
//					result.code = e.getReason();
//					result.message = "Bucket Creation Failed";
//					
//					log.warning("Bucket Creation Failed - Status: " + e.getReason());
//					
//				}


		} catch (Exception e) {
			Utils.logException(e);
			result.result = false;
			result.code = -1;
			result.message = "Internal Error.";
		}

		return result;

	}
	
//	@ApiMethod(
//			name = "subcompanies.get",
//			path = "subcompanies",
//			httpMethod = HttpMethod.GET
//	)
//	public ListResponse getSubcompanies(
//			@Named("companyId") String companyId, 
//			@Nullable @Named("search") String search,
//			@Nullable @Named("cursor") String cursor,
//			@Nullable @Named("count") String count,
//			@Nullable @Named("sort") String sort,
//			User user) {
//		
//		ListResponse result = new ListResponse();
//		List<Company> items = new ArrayList<Company>();
//		result.items = items;
//		
//		if (user == null) {
//			result.message = "No user";
//			return result;
//		}
//
//		log.info("User: " + user.getEmail());
//		
//		com.risevision.directory.documents.Company company = com.risevision.directory.documents.Company.get(companyId);
//		
//		if (company == null) {
//			result.message = "No company";
//			return result;
//		}
//		
//		log.info("Company: " + company.name);
//		
//		search = search != null ? search : "";
//    	int limit = Utils.safeParseInt(count, 50);
//    	sort = sort != null ? sort : com.risevision.directory.documents.Company.NAME + " asc" ;
//    	
//    	Results<ScoredDocument> results = null;
//	
//    	try {
//    		
//    		results = Utils.getDocuments(com.risevision.directory.documents.Company.class, String.format(com.risevision.directory.documents.Company.INDEX_SUBCOMPANIES, companyId),
//    				cursor, limit, search, sort);
//    		
//    		for (ScoredDocument d : results) {
//
//    			com.risevision.directory.documents.Company item = new com.risevision.directory.documents.Company(d);
//        		items.add(new Company(item));
//    		}
//
//    	} catch (Exception e) {
//
//    		Utils.logException(e);
//    		result.result = false;
//    		result.code = -1;
//    		result.message = "Internal Error.";
//    	}
//    	
//       	result.result = true;
//    	result.code = 0;
//    	result.cursor = results != null && results.getCursor() != null ? results.getCursor().toWebSafeString() : "";
//	    return result;
//    	
//	}
//	
//	@ApiMethod(
//			name = "usercompanies.get",
//			path = "user/companies",
//			httpMethod = HttpMethod.GET
//	)
//	
//	public ListResponse getUserCompanies(
//			User user) {
//		
//		ListResponse result = new ListResponse();
//		
//		if (user == null) {
//			result.message = "No user";
//			return result;
//		}
//
//		log.info("User: " + user.getEmail());
//		
//		com.risevision.directory.documents.User dbUser = com.risevision.directory.documents.User.getByUsername(user.getEmail());
//		
//		if (dbUser == null) {
//			
//			log.severe("No user in DB?!");
//			result.message = "No user";
//			return result;
//		}
//		
//		com.risevision.directory.documents.Company company = com.risevision.directory.documents.Company.get(dbUser.companyId);
//		
//		if (company == null) {
//			log.severe("No user company in DB?!");
//			result.message = "No company";
//			return result;
//		}
//		
//		log.info("Company: " + company.name);
//		
//		Company c = new Company(company, dbUser);
//		
//		List<Company> items = new ArrayList<Company>();
//		items.add(c);
//		
//		
//		result.items = items;
//    	result.result = true;
//    	result.code = 0;
//    	
//	    return result;
//		
//	}
	
}
