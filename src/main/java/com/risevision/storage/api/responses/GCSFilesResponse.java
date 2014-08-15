package com.risevision.storage.api.responses;

import java.util.List;

import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.users.User;

public class GCSFilesResponse extends SimpleResponse {
	
	public List<StorageObject> files;
	
	public GCSFilesResponse(User user){
		
                  super(user);
		
		this.files = null;
	}

}
