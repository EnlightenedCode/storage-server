package com.risevision.medialibrary.server.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.risevision.medialibrary.server.Cacheable;
import com.risevision.medialibrary.server.EntityKind;
import com.risevision.medialibrary.server.Utils;
import com.risevision.medialibrary.server.entities.Company;

public class Companies implements Serializable, Cacheable {
	
	private static final long serialVersionUID = 8084189529308101452L;

	private static final String KEY_PREFIX = "l#companies_";
	
	public String companyId;
	public List<Company> companies;
		
	public Companies (String companyId, List<Company> companies) {
		
		this.companyId = companyId;
		this.companies = companies;
	}
	
	public String getId() {
		
		return companyId;
	}
	
	static public String getKeyPrefix() {
		
		return KEY_PREFIX;
	}
	
	static public Companies recreate(String companyId) {
		
		Company company = Company.get(companyId);
		
		if (company == null)
			return null;
		
		return recreate(company.key);
	}
	
	static public Companies recreate(Key key) {
		
		ArrayList<Company> companies = new ArrayList<Company>();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query qq = new Query(EntityKind.COMPANY).setAncestor(key);
		
		Logger.getAnonymousLogger().warning("Retrieved from DB.");

		List<Entity> children = datastore.prepare(qq).asList(FetchOptions.Builder.withDefaults().chunkSize(1000).prefetchSize(1000));
		
		Logger.getAnonymousLogger().warning("Companies found:" + children.size());

		for (Entity ce : children) {
			
			companies.add(new Company(ce.getKey(), ce.getProperties()));
		}
		
//		Logger.getAnonymousLogger().info("Retrieved from DB.");
//		
//		for (Map.Entry<Key, Map<String, Object>> entry : tmp.entrySet()) {
//			
//			try {
//				
//				data.add(new Company(entry.getKey(), entry.getValue()));
//			
//			} catch (Exception e) {
//
//				Logger log = Logger.getAnonymousLogger();
//				log.severe("Error: " + e.toString());
//				log.info("Key: " + entry.getKey().toString());
//				Utils.logStackTrace(e, log);
//			}
//			
//		}
				
		return new Companies(Utils.getCompanyId(key), companies);
	}
	
}
