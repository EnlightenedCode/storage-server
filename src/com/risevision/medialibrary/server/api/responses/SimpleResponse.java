package com.risevision.medialibrary.server.api.responses;

public class SimpleResponse {
	
	public Boolean result;
	public Integer code;
	public String message;
	
	public SimpleResponse(){
		
		this.result = false;
		this.code = -1;
		this.message = "";
	}
	
	public SimpleResponse(Boolean result, Integer code, String message) {
		
		this.result = result;
		this.code = code;
		this.message = message;
	}

}

