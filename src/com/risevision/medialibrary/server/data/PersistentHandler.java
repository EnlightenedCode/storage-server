package com.risevision.medialibrary.server.data;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import com.risevision.medialibrary.server.data.PersistentOAuthInfo.OAuthType;
import com.risevision.medialibrary.server.utils.ServerUtils;

public class PersistentHandler {
	public static final Logger log = Logger.getLogger(PersistentHandler.class.getName());
	
//	public void saveConfig() {
//		saveConfig(new PersistentConfigurationInfo());
//	}
	
//	public void saveConfig(PersistentConfigurationInfo config) {
//        PersistenceManager pm = PMF.get().getPersistenceManager();
//
//        try {
//            pm.makePersistent(config);
//        }
//        finally {
//            pm.close();
//        }
//	}
	
	public PersistentConfigurationInfo getConfig() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        PersistentConfigurationInfo config = null;
        
        try {
        	config = pm.getObjectById(PersistentConfigurationInfo.class, PersistentConfigurationInfo.ENTITY_KEY);
        	log.config("Configuration parameters loaded from data store.");
        	
        	return config;
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
			
			return null;
		}
        finally {
        	pm.close();
        }
	}
	
//	public void saveUser(PersistentUserInfo user) {
//        PersistenceManager pm = PMF.get().getPersistenceManager();
//
//        try {
//            pm.makePersistent(user);
//        } finally {
//            pm.close();
//        }
//	}
	
	public PersistentUserInfo getUser() {
		return getUser(ServerUtils.getGoogleUsername());
	}
	
	public PersistentUserInfo getUser(String username) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        PersistentUserInfo user = null;
        
        try {
        	user = pm.getObjectById(PersistentUserInfo.class, username);
        	log.config("Username - " + username + " - token/secret loaded from data store.");

        	return user;
		}
		catch (Exception e) {
			return null;
		}
        finally {
        	pm.close();
        }
	}
	
//	public void saveOAuth(PersistentOAuthInfo oAuth) {
//        PersistenceManager pm = PMF.get().getPersistenceManager();
//
//        try {
//            pm.makePersistent(oAuth);
//        } finally {
//            pm.close();
//        }
//	}
	
	public PersistentOAuthInfo getOAuth(OAuthType oAuthType) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        PersistentOAuthInfo oAuth = null;
        
        try {
        	oAuth = pm.getObjectById(PersistentOAuthInfo.class, oAuthType.getEntityKey());
        	log.config("OAuth parameters loaded from data store.");
        	
        	return oAuth;
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
			
			return null;
		}
        finally {
        	pm.close();
        }
	}
}
