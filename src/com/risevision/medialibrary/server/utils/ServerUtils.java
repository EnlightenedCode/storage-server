package com.risevision.medialibrary.server.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.risevision.medialibrary.server.data.DataService;
import com.risevision.medialibrary.server.data.PersistentConfigurationInfo;
import com.risevision.medialibrary.server.data.PersistentOAuthInfo;
import com.risevision.medialibrary.server.data.PersistentUserInfo;
import com.risevision.medialibrary.server.data.PersistentOAuthInfo.OAuthType;
import com.risevision.medialibrary.server.info.ServiceFailedException;

public class ServerUtils {

	public static Date strToDate(String value, Date defaultValue) {
		try {			
			SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"); // RFC-822 date-time with time zone  
			return df.parse(value);        			
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static String dateToRfc822(Date date) {
		
    	if (date != null) {
    		SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"); // RFC-822 date-time with time zone 
    		return formatter.format(date);
    	} else {
    		return "";
    	}
	}
	
	public static Date strToDate(String value) {
		return strToDate(value, null);
	}
	
	public static boolean isUserLoggedIn() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.isUserLoggedIn();
	}
	
	public static String getGoogleUsername() {
		String username = null;
		UserService userService = UserServiceFactory.getUserService();
		if (userService.isUserLoggedIn()) {
			User currentUser = userService.getCurrentUser();
			username = currentUser.getEmail();
	    }
		else {
			return null;
		}
		return username;
	}		
	
	public static PersistentUserInfo getPersistentUser() throws ServiceFailedException {
		PersistentUserInfo user = DataService.getInstance().getUser();
		
		if (user == null) {
			throw new ServiceFailedException(ServiceFailedException.AUTHENTICATION_FAILED);
		}
		
		return user;
	}
	
	public static String formatUrl(String url, String urlPath) {
		PersistentConfigurationInfo pConfig = DataService.getInstance().getConfig();
		
		url = pConfig.getServerURL() + "/" + urlPath + url;
		
//		if (url.contains("?")) 
//			url += "&";
//		else
//			url += "?";
//		
//		url += "appId=rdntwo";
		
		return url;
	}
	
	private static OAuthConsumer createConsumer(PersistentUserInfo user) throws ServiceFailedException {	
		PersistentOAuthInfo oAuth = DataService.getInstance().getOAuth(OAuthType.user);
		OAuthConsumer consumer = new DefaultOAuthConsumer(oAuth.getConsumerKey(), oAuth.getConsumerSecret());
		
		consumer.setTokenWithSecret(user.getUserToken(), user.getUserTokenSecret());
		
		return consumer;
	}
	
	public static void signResource(ClientResource clientResource, String url, String method) throws ServiceFailedException {
		PersistentUserInfo user = getPersistentUser();
		OAuthConsumer consumer = createConsumer(user);
		
		try {
			URL payload_url = new URL(url);
//			URL payload_url = new URL(ServerUtils.formatUrl(url));
			
			HttpURLConnection r = (HttpURLConnection) payload_url.openConnection();
			r.setRequestMethod(method);
	
	        // sign the request
	        try {
				consumer.sign(r);
			} catch (OAuthMessageSignerException e) {
	//			log.severe(e.getMessage());
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
	//			log.severe(e.getMessage());
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
	//			log.severe(e.getMessage());
				e.printStackTrace();
			}
				
			String header = "";
			try {
				header = r.getRequestProperty(OAuth.HTTP_AUTHORIZATION_HEADER);
				if (header.contains("OAuth ")) {
					header = header.substring("OAuth ".length(), header.length());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_OAUTH);
			challenge.setRawValue(header);
			
			clientResource.setChallengeResponse(challenge);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
