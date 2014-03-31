package com.risevision.storage.cache;

import java.io.Serializable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.risevision.storage.CacheUtils;
import com.risevision.storage.Cacheable;
import com.risevision.storage.EntityKind;
import com.risevision.storage.Globals;
import com.risevision.storage.entities.Company;

public class Root implements Serializable, Cacheable {
	
	private static final long serialVersionUID = 1892717684789795252L;
	
	private static final String KEY_PREFIX = "##root_";
	private static final String KEY_ID = "company";
	
	public String companyId;
	
	public Root (String companyId) {

		this.companyId = companyId;
	}

	public String getId() {

		return KEY_ID;
	}
	
	public static String getCompanyId() {
		
		Root root = CacheUtils.get(Root.class, KEY_ID);
		return root != null ? root.companyId : null;
	}

	// ************************* Cacheable support *******************************
	
	static public String getKeyPrefix() {

		return KEY_PREFIX;
	}
	
	static public Root recreate(String id) {
		
		Root result = null;

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Key rootKey = KeyFactory.createKey(EntityKind.COMPANY, Globals.ROOT);
		
		Entity root = null;
		
		try {
			
			root = datastore.get(rootKey);
		
		} catch (EntityNotFoundException e) {
			
			root = null;
		}
		
		if (root != null) {

			String rootId = (String)root.getProperty(Company.ID);
			result = new Root(rootId);
		}

		return result;
	}
	

}
