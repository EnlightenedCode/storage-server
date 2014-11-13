package com.risevision.storage.security;

import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.risevision.directory.documents.Company;
import com.risevision.directory.documents.User;
import com.risevision.storage.info.ServiceFailedException;

public class AccessResource {
	
	private String authKey;
	private String companyId;
	
	private User accessingUser;
	private String accessingUserId;
	private Company accessingUserCompany;
	private Company resourceCompany;
  private String sharedFolder;
	
	private int status;
	
	protected static final Logger log = Logger.getAnonymousLogger();
	
	public AccessResource(String authKey) {
		this.authKey = authKey;
	}
	
	public AccessResource(String companyId, String username) {
		this.companyId = companyId;
		this.accessingUserId = username;		
	}

  public AccessResource(String companyId, String username, String sharedFolder) {
    this.companyId = companyId;
    this.accessingUserId = username;
    this.sharedFolder = (sharedFolder == null || sharedFolder.equals("")) ? "" : sharedFolder.substring(0,sharedFolder.lastIndexOf("/") + 1);
  }
	public void verify() throws ServiceFailedException {  
		if (!Strings.isNullOrEmpty(authKey)) {
			verifyByKey();
		}
		else {
			verifyByUser();
		}
	}
	
	private void verifyByKey() throws ServiceFailedException {
		resourceCompany = Company.getByAuthKey(authKey);
		
    	if (resourceCompany != null) {
    		
    		log.info("Resource Company: " + resourceCompany.name + " (id: " + resourceCompany.id + ")");
    		
    	}
    	
		if (!AccessVerifier.AuthKeyAllowed(this)) {
			
			throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
			
//			setStatus(ServiceFailedException.FORBIDDEN);
//    		return;

		} else {
			
			setStatus(ServiceFailedException.OK);
			
		}
	}
	
	private void verifyByUser() throws ServiceFailedException {
		if (Strings.isNullOrEmpty(companyId)) {
			throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
		}
		
		if (Strings.isNullOrEmpty(accessingUserId)) {
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

  public String getSharedFolder() {
    return sharedFolder;
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
	
	public String getResourceCompanyId() {
		if (resourceCompany != null) {
			return resourceCompany.getId();
		}
		
		return null;
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
