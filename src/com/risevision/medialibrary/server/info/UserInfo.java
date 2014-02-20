package com.risevision.medialibrary.server.info;

import java.io.Serializable;
import java.util.ArrayList;

import com.risevision.medialibrary.server.utils.ServerUtils;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {

	private String username;
	private ArrayList<CompanyInfo> companies = new ArrayList<CompanyInfo>();
	
	public UserInfo() {
		username = ServerUtils.getGoogleUsername();
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public ArrayList<CompanyInfo> getCompanies() {
		return companies;
	}
	
	public void setCompanies(ArrayList<CompanyInfo> companies) {
		this.companies = companies;
	}
	
}
