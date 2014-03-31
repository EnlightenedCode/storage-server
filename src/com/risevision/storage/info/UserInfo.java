package com.risevision.storage.info;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {

	private String username;
	private ArrayList<CompanyInfo> companies = new ArrayList<CompanyInfo>();
	
	public UserInfo(String username) {
		this.username = username;
	}
	
	public CompanyInfo getCompany(String companyId) {
		CompanyInfo company = null;
		for (CompanyInfo item: companies) {
			if (item.getId().equals(companyId)) {
				company = item;
				
				break;
			}
		}
		
		return company;
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
