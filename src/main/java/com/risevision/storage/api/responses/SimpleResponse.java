package com.risevision.storage.api.responses;
import com.google.appengine.api.users.User;
import java.util.logging.Logger;

public class SimpleResponse {
	
	public Boolean result;
	public Integer code;
	public String message;
  public String userEmail;

  private static final Logger log = Logger.getAnonymousLogger();

	public SimpleResponse(){
		
		this.result = false;
		this.code = -1;
		this.message = "";
                this.userEmail = null;
	}
	
	public SimpleResponse(Boolean result, Integer code, String message) {
		this.result = result;
		this.code = code;
		this.message = message;
	}
  
  public SimpleResponse(Boolean result, Integer code, String message, String userEmail) {
    this.result = result;
    this.code = code;
    this.message = message;
    this.userEmail = userEmail;
  }

  public SimpleResponse(User user) {
    if (user == null) {
      throw new IllegalArgumentException("No user");
    }

    this.userEmail = user.getEmail();
    log.info("User: " + this.userEmail);
  }
}
