package com.risevision.medialibrary.server.service;

import java.util.logging.Logger;

import com.risevision.medialibrary.server.data.CacheHandler;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.info.UserInfo;
import com.risevision.medialibrary.server.utils.ServerUtils;

public class AuthenticationService {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void checkAuthorization(String companyId) throws ServiceFailedException {
		if (ServerUtils.isUserLoggedIn()) {
			UserInfo user = getUser();
			CompanyInfo company = null;
			if (user != null) {
				log.info("User found in cache");

				for (CompanyInfo item: user.getCompanies()) {
					if (item.getId().equals(companyId)) {
						company = item;
						
						log.info("User Company found in cache");
						
						break;
					}
				}
			}
			else {
				user = new UserInfo();
				
				log.warning("User not found in cache");
			}
			
			if (company == null) {
				try {
					log.warning("Retrieving Company record");

					company = new CompanyService().getCompany(companyId);
				} catch (ServiceFailedException e) {
					// [AD] ideally we should just throw the exception; however since we need to add the response to 
					// memcache, we will catch and re-throw it after processing
				}

				if (company == null) {
					company = new CompanyInfo();
					company.setId(companyId);
					company.setAuthorized(false);
				}
				
				user.getCompanies().add(company);
				saveUser(user);
				
			}
			
			if (company != null && !company.isAuthorized()) {				
				throw new ServiceFailedException(ServiceFailedException.FORBIDDEN);
			}
			else if (company != null && !company.isMediaLibraryEnabled()) {
				throw new ServiceFailedException(ServiceFailedException.PRECONDITION_FAILED);
			}
		}
		else {
			throw new ServiceFailedException(ServiceFailedException.AUTHENTICATION_FAILED);
		}
		
	}
	
	private UserInfo getUser() {
		String username = ServerUtils.getGoogleUsername();
		String key = getCacheItemName(username);
		CacheHandler cache = CacheHandler.getInstance();
		
		UserInfo user = (UserInfo) cache.findInCache(key);

		return user;
	}
	
	private void saveUser(UserInfo user) {
		String username = ServerUtils.getGoogleUsername();
		String key = getCacheItemName(username);
		CacheHandler cache = CacheHandler.getInstance();

		if (user != null) {
			cache.putInCache(key, user);
		}		
	}
	
	private String getCacheItemName(String username) {
		return "UserInfo:" + username;
	}
}
