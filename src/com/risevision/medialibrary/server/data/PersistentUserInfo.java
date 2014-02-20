package com.risevision.medialibrary.server.data;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.risevision.medialibrary.server.utils.ServerUtils;

@SuppressWarnings("serial")
@PersistenceCapable
public class PersistentUserInfo implements Serializable {
	@Persistent
	private String id;
	
	@Persistent
	@PrimaryKey
	private String userName;
	@Persistent
	private String company;

	@Persistent
	private String userToken;
	@Persistent
	private String userTokenSecret;
	
	public PersistentUserInfo(String userToken, String userTokenSecret) {
		this.userToken = userToken;
		this.userTokenSecret = userTokenSecret;
		this.userName = ServerUtils.getGoogleUsername();
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getUserToken() {
		return userToken;
	}
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	public String getUserTokenSecret() {
		return userTokenSecret;
	}
	public void setUserTokenSecret(String userTokenSecret) {
		this.userTokenSecret = userTokenSecret;
	}

}
