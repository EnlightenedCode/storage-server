package com.risevision.storage.gcs;

import java.io.IOException;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.storage.Storage;

public class StorageHelper {

	private static Storage storage;
	
	public static Storage getStorage(String userId) throws IOException {
	
		if (storage == null) {
		    // Get the stored credentials using the Authorization Flow
		    AuthorizationCodeFlow authFlow = AuthUtils.initializeFlow();
		    Credential credential = authFlow.loadCredential(userId);
		    
		    storage = new Storage.Builder(AuthUtils.HTTP_TRANSPORT, AuthUtils.JSON_FACTORY, credential)
		    		.setApplicationName("").build();
	    
		}

	    return storage;

	}	
	
}
