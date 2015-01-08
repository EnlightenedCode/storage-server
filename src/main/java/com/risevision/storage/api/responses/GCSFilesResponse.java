package com.risevision.storage.api.responses;

import java.util.List;

import com.google.appengine.api.users.User;

public class GCSFilesResponse extends SimpleResponse {
  public List<?> files;

  public GCSFilesResponse(){
    super();
    this.files = null;
  }

  public GCSFilesResponse(User user) {
    super(user);
	  
    this.files = null;
  }

  public GCSFilesResponse(User user, Boolean result, Integer code, List<?> files) {
    super(user);
    
    this.result = result;
    this.code = code;
    this.files = files;
  }
}
