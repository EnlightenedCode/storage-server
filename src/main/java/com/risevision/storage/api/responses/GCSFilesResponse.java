package com.risevision.storage.api.responses;

import java.util.List;

import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.users.User;
import com.risevision.storage.entities.ShareFolder;
import org.json.JSONArray;

public class GCSFilesResponse extends SimpleResponse {
	
	public List<StorageObject> files;
    public List<ShareFolder> sharedFolders;
	
	public GCSFilesResponse(User user){
		
                  super(user);
		
		this.files = null;
        this.sharedFolders = null;
	}

}
