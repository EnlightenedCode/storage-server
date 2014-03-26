package com.risevision.medialibrary.server.api.responses;

import java.util.List;

import com.risevision.medialibrary.server.info.MediaItemInfo;

public class FilesResponse extends SimpleResponse {
	
	public List<MediaItemInfo> files;
	
	public FilesResponse(){
		
		super();
		
		this.files = null;
	}

}
