package com.risevision.storage.security;

import java.util.logging.Logger;

import com.risevision.core.api.types.CompanyStatus;
import com.risevision.core.api.types.UserRole;
import com.risevision.core.api.types.UserStatus;
import com.risevision.directory.documents.Company;
import com.risevision.directory.documents.User;

public class AccessVerifier {
	
//	private static final String INTERNAL = "rdn-openid.appspot.com"; // change to actual key!
//	private static final String ANONYMOUS = "anonymous";
	
	protected static final Logger log = Logger.getAnonymousLogger();
		
//	static public boolean ConsumerAppAllowed(AbstractEntityResource resource) {
//		
//		String ckey = resource.getConsumerKey();
//		
//		// no key = FAIL
//		if (ckey == null || ckey.isEmpty()) {
//			log.warning("App is not allowed: no consumer key.");
//			return false;
//		}
//		
//		// our own key = PASS
//		if (ckey.equals(INTERNAL))
//			return true;
//						
//		// anonymous key = FAIL
//		if (ckey.equals(ANONYMOUS)) {
//			log.warning("App is not allowed: anonymous consumer key.");
//			return false;
//		}
//		
//		Company company = resource.getResourceCompany();
//		
//		
//		// SECURITY EXCEPTION: ALLOW user lookup without resource company
////		if ((resource instanceof UserLoginResource) && resource.getMethod().equals(Method.GET) && company == null) {
////			return true;
////		}
//		
//		// no company = FAIL
//		if (company == null) {
//			log.warning("App is not allowed: no resource company to verify against.");
//			return false;
//		}
//		
//		// consumer key is assigned to the company = PASS
//		if (company.oauthConsumerKeys != null && company.oauthConsumerKeys.contains(ckey))
//			return true;
//		
//		String parentId = company.parentId;
//		
//		while (parentId != null && !parentId.isEmpty()) {
//		
//			company = Company.get(parentId);
//			
//			// no company = FAIL
//			if (company == null) {
//				log.warning("App is not allowed: no parent company to verify against.");
//				return false;
//			}
//			
//			// consumer key is assigned to some parent company = PASS
//			if (company.oauthConsumerKeys != null && company.oauthConsumerKeys.contains(ckey))
//				return true;
//			
//			parentId = company.parentId;
//			
//		} 
//		
//		// no parent has the needed consumer key = FAIL
//		log.warning("App is not allowed: no parent has the needed consumer key.");
//		return false;
//    
//	}
	
	static public boolean UserAllowed(AccessResource resource) {
		
		User user = resource.getAccessingUser();
		Company resourceCompany = resource.getResourceCompany();

		// no user = FAIL
		if (user == null) {
			log.warning("User is not allowed: no resource user.");
			return false;
		}
		
    	// user is inactive = FAIL
    	if (user.status == UserStatus.INACTIVE) { // PRIMITIVE TYPE EQUALITY!!! 
    		log.warning("User is not allowed: user is inactive.");
    		return false;
    	}
  	
    	Company userCompany = resource.getAccessingUserCompany();
    	
    	// no user's company in the DB = FAIL
    	if (userCompany == null) {
    		log.warning("User is not allowed: no user's company in the DB.");
    		return false;
    	}
		
    	// user's company is inactive = FAIL
    	if (userCompany.companyStatus == CompanyStatus.INACTIVE) { // PRIMITIVE TYPE EQUALITY!!!
    		log.warning("User is not allowed: user's company is inactive.");
    		return false;
    	}

		// no resource company = FAIL
		if (resourceCompany == null) {
			log.warning("User is not allowed: no resource company.");
			return false;
		}
    	
    	// resource's company is user's company = PASS
		if (user.companyId.equals(resourceCompany.id))
			return true;
		
		String parentId = resourceCompany.parentId;
		
		// parent company is user's company = PASS
		if (user.companyId.equals(parentId))
			return true;
		
		Company parentCompany;
		
		while (parentId != null && !parentId.isEmpty()) {
		
			parentCompany = Company.get(parentId);
			
			// no parent company = FAIL
			if (parentCompany == null) {
				log.warning("User is not allowed: no parent company in the DB.");
				return false;
			}
			
			parentId = parentCompany.parentId;
			
			// some parent company is user's company = PASS
			if (user.companyId.equals(parentId))
				return true;
		} 
		
		// no parent was user's company = FAIL
		log.warning("User is not allowed: no parent was user's company.");
		return false;

	}

	// special case
//	static public boolean CanAccessCompany(CompanyResource resource) {
//	
//		User user = resource.getAccessingUser();
//		
//		// SECURITY EXCEPTION: ALLOW the yet-unregistered user to register their company
//		if (resource.getMethod().equals(Method.PUT) && user == null && resource.getResourceCompany() == null) {
//			return true;
//		}
//		
//    	// no user = FAIL
//		if (user == null)
//			return false;
//    	
//    	// user is inactive = FAIL
//    	if (user.status == UserStatus.INACTIVE) // PRIMITIVE TYPE EQUALITY!!!
//    		return false;
//    	
//    	Company userCompany = resource.getAccessingUserCompany();
//    	
//    	// no user's company in the DB = FAIL
//    	if (userCompany == null)
//    		return false;
//		
//    	// user's company is inactive = FAIL
//    	if (userCompany.companyStatus == CompanyStatus.INACTIVE) // PRIMITIVE TYPE EQUALITY!!!
//    		return false;
//		
//		Company company = resource.getResourceCompany();
//		
//		// new company
//		if (company == null)
//			return true;
//		
//    	// resource's company is user's company = PASS
//		if (user.companyId.equals(company.id))
//			return true;
//		
//		String parentId = company.parentId;
//		
//		// parent company is user's company = PASS
//		if (user.companyId.equals(parentId))
//			return true;
//		
//		while (parentId != null && !parentId.isEmpty()) {
//		
//			company = Company.get(parentId);
//			
//			// no company = FAIL
//			if (company == null)
//				return false;
//			
//			parentId = company.parentId;
//			
//			// some parent company is user's company = PASS
//			if (user.companyId.equals(parentId))
//				return true;
//		} 
//		
//		// no parent was user's company = FAIL
//		return false;
//
//	}
	
	static public boolean CanPut(AccessResource resource, int token) {
	
		
		User user = resource.getAccessingUser();
		
    	// no user in DB = FAIL
    	if (user == null) {
    		//log.warning("User is not allowed to put: no user in the DB.");
    		return false;
    	}
		
    	// FAIL by default
    	boolean result = false;
    	
    	switch (token) {

    	case AccessToken.COMPANY_SETTINGS:
    		//result = user.getRoles().contains(UserRole.SYSTEM_ADMINISTRATOR);
    		result = user.roles.contains(UserRole.USER_ADMINISTRATOR); // as per Robb on 20-Oct-2010
    		break;

    	case AccessToken.COMPANY:
    		result = user.roles.contains(UserRole.USER_ADMINISTRATOR);
    		break;

    	case AccessToken.USER:
    		result = user.roles.contains(UserRole.USER_ADMINISTRATOR);
    		break;

    	case AccessToken.DISPLAY:
    		result = user.roles.contains(UserRole.DISPLAY_ADMINISTRATOR);
    		break;
    		
		case AccessToken.SCHEDULE:
			result = user.roles.contains(UserRole.CONTENT_PUBLISHER);
			break;
			
		case AccessToken.PRESENTATION:
			result = user.roles.contains(UserRole.CONTENT_PUBLISHER) || user.roles.contains(UserRole.CONTENT_EDITOR);
			break;
			
		case AccessToken.PRESENTATION_PUBLISH:
			result = user.roles.contains(UserRole.CONTENT_PUBLISHER);
			break;
					
		case AccessToken.GADGET:
			result = user.roles.contains(UserRole.CONTENT_PUBLISHER);
			break;
			
		case AccessToken.DEMO:
			result = user.roles.contains(UserRole.CONTENT_PUBLISHER);
			break;
    	}

		return result;

	}
	
}
