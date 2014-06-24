package com.risevision.storage.api.responses;

import java.util.List;

import com.google.api.services.storage.model.StorageObject;

public class GCSFilesResponse extends SimpleResponse {
	
	public List<StorageObject> files;
	
	public GCSFilesResponse(){
		
		super();
		
		this.files = null;
	}

}
