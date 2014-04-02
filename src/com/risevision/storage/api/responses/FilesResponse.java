package com.risevision.storage.api.responses;

import java.util.List;

import com.risevision.storage.info.MediaItemInfo;

public class FilesResponse extends SimpleResponse {
	
	public List<MediaItemInfo> files;
	
	public FilesResponse(){
		
		super();
		
		this.files = null;
	}

}
