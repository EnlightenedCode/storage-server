package com.risevision.medialibrary.server.utils;

import java.util.logging.Logger;

import com.risevision.medialibrary.server.data.CacheHandler;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.info.UserInfo;
import com.risevision.medialibrary.server.service.CompanyService;

public class CacheUtils {
	protected static final Logger log = Logger.getAnonymousLogger();

	public static CompanyInfo getUserCompany(String companyId, String username) throws ServiceFailedException {
		UserInfo user = getCurrentUser(username);
		CompanyInfo company = user.getCompany(companyId);

		if (company != null) {
			
			log.info("User Company found in cache");
		
		}
		else if (company == null) {
			
			try {
				log.warning("Retrieving Company record");

				company = CompanyService.getInstance().getCompany(companyId, username);
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
			CacheUtils.saveUser(user);
			
		}
		
		return company;
		
	}
	
	public static void updateCompany(CompanyInfo company, String username) {
		UserInfo user = getUser(username);
		
		if (user.getCompany(company.getId()) == null) {
			user.getCompanies().add(company);
		}
		else {
			user.getCompanies().remove(user.getCompany(company.getId()));
			user.getCompanies().add(company);
		}
		
		CacheUtils.saveUser(user);
	}
	
	private static UserInfo getCurrentUser(String username) {
		UserInfo user = getUser(username);
		if (user != null) {
			log.info("User found in cache");
		}
		else {
			user = new UserInfo(username);
			
			log.warning("User not found in cache");
		}
		
		return user;
	}
	
	private static UserInfo getUser(String username) {
		String key = getCacheItemName(username);
		CacheHandler cache = CacheHandler.getInstance();
		
		UserInfo user = (UserInfo) cache.findInCache(key);

		return user;
	}
	
	private static void saveUser(UserInfo user) {
		String key = getCacheItemName(user.getUsername());
		CacheHandler cache = CacheHandler.getInstance();

		if (user != null) {
			cache.putInCache(key, user);
		}		
	}
	
	private static String getCacheItemName(String username) {
		return "UserInfo:" + username;
	}
	
}
