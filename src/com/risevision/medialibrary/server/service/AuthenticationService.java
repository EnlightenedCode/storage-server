package com.risevision.medialibrary.server.service;

import java.util.logging.Logger;

import com.risevision.medialibrary.server.data.CacheHandler;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.info.UserInfo;
import com.risevision.medialibrary.server.utils.ServerUtils;

public class AuthenticationService {
	protected static final Logger log = Logger.getAnonymousLogger();

	public boolean isAuthorized(String companyId) {
		if (ServerUtils.isUserLoggedIn()) {
			UserInfo user = getUser();
			CompanyInfo company = null;
			if (user != null) {
				log.warning("User found in cache");

				for (CompanyInfo item: user.getCompanies()) {
					if (item.getId().equals(companyId)) {
						company = item;
						
						log.warning("User Company found in cache");
						
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

				}

				if (company == null) {
					company = new CompanyInfo();
					company.setId(companyId);
					company.setAuthorized(false);
				}
				
				user.getCompanies().add(company);
				saveUser(user);
				
			}
			
			if (company != null) {
				log.warning("User is " + (company.isAuthorized() ? "" : "NOT") + " Authorized");

				return company.isAuthorized();
			}
		}
		
		return false;
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
