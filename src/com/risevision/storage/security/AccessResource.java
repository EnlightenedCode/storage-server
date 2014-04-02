package com.risevision.storage.security;

import java.util.logging.Logger;

import com.risevision.common.client.utils.RiseUtils;
import com.risevision.directory.documents.Company;
import com.risevision.directory.documents.User;
import com.risevision.storage.info.ServiceFailedException;

public class AccessResource {
	
	private String companyId;
	
	private User accessingUser;
	private String accessingUserId;
	private Company accessingUserCompany;
	private Company resourceCompany;
	
	private int status;
	
	protected static final Logger log = Logger.getAnonymousLogger();
	
	public AccessResource(String companyId, String username) {
		this.companyId = companyId;
		this.accessingUserId = username;		
	}
	
	public void verify() throws ServiceFailedException {  
		
		if (RiseUtils.strIsNullOrEmpty(companyId)) {
			throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
		}
		
		if (RiseUtils.strIsNullOrEmpty(accessingUserId)) {
			throw new ServiceFailedException(ServiceFailedException.AUTHENTICATION_FAILED);
		}
		
		resourceCompany = Company.get(companyId);
		
		accessingUser = null;
		accessingUserCompany = null;
		
//    	Form form = getRequest().getResourceRef().getQueryAsForm(); 
//    	String debug = form.getFirstValue("debug");
//    	if (debug != null && debug.equals("noauth")) {
//    		
//    		log.warning("Debugging mode: no authentication!");
//    		setStatus(Status.SUCCESS_OK);
//    		setConditional(false);
//    		return;
//    	}
    	    	
//    	com.google.appengine.api.users.User googleUser = null;
//
//    	try {
//    		
//    		OAuthService oauthService = OAuthServiceFactory.getOAuthService();
//    		googleUser = oauthService.getCurrentUser();
//    		log.info("GoogleAccounts user: " + (googleUser != null ? googleUser.getEmail() : "NULL?!"));
//    		
//    	} catch (OAuthRequestException e) {
//    		log.severe("Cannot obtain GoogleAccounts user: " + e.toString() + ", " +  e.getMessage());
//    		Utils.logException(e);
//    		googleUser = null;
//    	}
//     	
//    	if (googleUser == null || googleUser.getEmail() == null || googleUser.getEmail().isEmpty()) {
//    	
//    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
//    		return;
//    	}
//    	
//    	accessingUserId = googleUser.getEmail();
		
    	accessingUser = findAccessingUser(accessingUserId);
    	accessingUserCompany = accessingUser != null ? Company.get(accessingUser.companyId) : null;
    	
    	if (accessingUser != null) {
    		log.info("Accessing RVA user: " + accessingUser.username + " (id: " + accessingUser.id + ")");
    	}
		
    	if (accessingUserCompany != null) {
    		
    		log.info("Accessing user's company: " + accessingUserCompany.name + " (id: " + accessingUserCompany.id + ")");
    	}
    	
//		if (!AccessVerifier.ConsumerAppAllowed(this)) {
//		
//			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
//    		return;
//				
//		} else 
		if (!AccessVerifier.UserAllowed(this)) {
			
			throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
			
//			setStatus(ServiceFailedException.FORBIDDEN);
//    		return;

		} else {
			
			setStatus(ServiceFailedException.OK);
		}
     	
    }
	
	public String getCompanyId() {  
    	return companyId;
    } 
	
	public User getAccessingUser() {

		return accessingUser;
	}
	
	public String getAccessingUserId() {

		return accessingUserId;
	}

	public Company getAccessingUserCompany() {

		return accessingUserCompany;
	}

	public Company getResourceCompany() {

		return resourceCompany;
	}
	
	private User findAccessingUser(String googleEmail) {
				
		return User.getByUsername(googleEmail);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
