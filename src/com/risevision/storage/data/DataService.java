package com.risevision.storage.data;

import com.risevision.storage.data.PersistentOAuthInfo.OAuthType;

public class DataService {
	private PersistentHandler persistentHandler = new PersistentHandler();
	private static DataService instance;
	
	private DataService() {
	
	}
	
	public static synchronized DataService getInstance() {
		if (instance == null) {
			instance = new DataService();
		}
		return instance;
	}
	
//	public void saveConfig() {
//		persistentHandler.saveConfig();
//	}
		
	public PersistentConfigurationInfo getConfig() {
		CacheHandler cache = CacheHandler.getInstance();
		
		PersistentConfigurationInfo config = (PersistentConfigurationInfo) cache.findInCache(PersistentConfigurationInfo.ENTITY_KEY);
		
		if (config == null) {
			config = persistentHandler.getConfig();
			
			if (config == null) {
				config = new PersistentConfigurationInfo();
//				persistentHandler.saveConfig(config);
			}
			
			cache.putInCache(PersistentConfigurationInfo.ENTITY_KEY, config);
		}
		
//		return new PersistentConfigurationInfo();
		return config;
	}
	
//	public void saveUser(PersistentUserInfo user) {
//		persistentHandler.saveUser(user);
//		
//		CacheHandler cache = CacheHandler.getInstance();
//
//		cache.putInCache(user.getUserName(), user);
//	}
	
	public PersistentUserInfo getUser(String username) {
		CacheHandler cache = CacheHandler.getInstance();
		
		PersistentUserInfo user = (PersistentUserInfo) cache.findInCache(username);
		
		if (user == null) {
			user = persistentHandler.getUser(username);
			
			if (user != null) {
				cache.putInCache(username, user);
			}
		}

		return user;
	}
	
//	public void saveOAuth(PersistentOAuthInfo oAuth) {
//		persistentHandler.saveOAuth(oAuth);
//	}
		
	public PersistentOAuthInfo getOAuth(OAuthType oAuthType) {
		CacheHandler cache = CacheHandler.getInstance();
		
		PersistentOAuthInfo oAuth = (PersistentOAuthInfo) cache.findInCache(oAuthType.getEntityKey());
		
		if (oAuth == null) {
			oAuth = persistentHandler.getOAuth(oAuthType);
			
			if (oAuth == null) {
				oAuth = new PersistentOAuthInfo(oAuthType);
//				persistentHandler.saveOAuth(oAuth);
			}
			cache.putInCache(oAuthType.getEntityKey(), oAuth);
		}
		
		return oAuth;
	}

}
