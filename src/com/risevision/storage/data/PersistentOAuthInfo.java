package com.risevision.storage.data;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@SuppressWarnings("serial")
@PersistenceCapable
public class PersistentOAuthInfo implements Serializable {
	public static final	String TWITTER_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	public static final	String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	public static final	String TWITTER_AUTHORIZE_TOKEN_URL = "https://api.twitter.com/oauth/authorize";
	
	@Persistent
	@PrimaryKey
	private String entityKey;
	@Persistent
	private String consumerKey;
	@Persistent
	private String consumerSecret;
	
	public enum OAuthType {
		user("OAuth", 
                "rdn-openid.appspot.com", 
                "biXlbnRjUJBmt7Iu0DJ26ugo"),
        twitter("TwitterOAuth", 
                "rAlUyAYxMDrnoiXIul03Q",
                "LbGXJGQ3huWOYEVmPGlqS6cBLgvGMTQBiXiblU2Swg");
		
		private String entityKeyName;
		private String defaultKey;
		private String defaultSecret;
		private OAuthType(String entityKeyName, String defaultKey, String defaultSecret) {
			this.entityKeyName = entityKeyName;
			this.defaultKey = defaultKey;
			this.defaultSecret = defaultSecret;
		}
		
		public String getEntityKey() {
			return entityKeyName;
		}

//		public String getDefaultKey() {
//			return defaultKey;
//		}
//		
//		public String getDefaultSecret() {
//			return defaultSecret;
//		}
		
	}
	
	public PersistentOAuthInfo(OAuthType oAuthType) {
		this.entityKey = oAuthType.entityKeyName;
		this.consumerKey = oAuthType.defaultKey;
		this.consumerSecret = oAuthType.defaultSecret;
	}
	
	public String getEntityKey() {
		return entityKey;
	}

	public String getConsumerKey() {
		return consumerKey;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

}
