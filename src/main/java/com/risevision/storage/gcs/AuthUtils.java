package com.risevision.storage.gcs;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.storage.StorageScopes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

class AuthUtils {

	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to
	 * make it a single globally shared instance across your application.
	 */
	private static final AppEngineDataStoreFactory DATA_STORE_FACTORY = 
			AppEngineDataStoreFactory.getDefaultInstance();

	private static GoogleClientSecrets clientSecrets = null;
	private static final Set<String> SCOPES = Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL);
	static final String MAIN_SERVLET_PATH = "/job/auth";
	static final String AUTH_CALLBACK_SERVLET_PATH = "/job/oauth2callback";
	static final UrlFetchTransport HTTP_TRANSPORT = new UrlFetchTransport();
	static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static GoogleClientSecrets getClientSecrets() throws IOException {
		if (clientSecrets == null) {
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(AuthUtils.class.getResourceAsStream("/client_secrets.json")));
			
			Preconditions.checkArgument(!clientSecrets.getDetails().getClientId()
									.startsWith("Enter ")
									&& !clientSecrets.getDetails().getClientSecret().startsWith("Enter "),
							"Download client_secrets.json file from https://code.google.com/apis/console/?api=plus "
									+ "into src/client_secrets.json");
		}
		return clientSecrets;
	}

	static GoogleAuthorizationCodeFlow initializeFlow() throws IOException {
		return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
				JSON_FACTORY, getClientSecrets(), SCOPES)
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("offline").build();
	}

	static String getRedirectUri(HttpServletRequest req) {
		GenericUrl requestUrl = new GenericUrl(req.getRequestURL().toString());
		requestUrl.setRawPath(AUTH_CALLBACK_SERVLET_PATH);
		return requestUrl.build();
	}
}