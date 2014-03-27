package com.risevision.medialibrary.server.service;

import java.util.logging.Logger;

import com.risevision.common.client.utils.RiseUtils;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.utils.CacheUtils;

public class AuthenticationService {
	protected static final Logger log = Logger.getAnonymousLogger();

	public static void checkAuthorization(String companyId, String username) throws ServiceFailedException {
		
		if (!RiseUtils.strIsNullOrEmpty(companyId)) {
			if (!RiseUtils.strIsNullOrEmpty(username)) {
				CompanyInfo company = CacheUtils.getUserCompany(companyId, username);
	
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
		else {
			throw new ServiceFailedException(ServiceFailedException.BAD_REQUEST);
		}
		
	}
	
}
