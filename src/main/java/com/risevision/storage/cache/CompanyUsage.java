package com.risevision.storage.cache;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.risevision.storage.Cacheable;
import com.risevision.storage.EntityKind;
import com.risevision.storage.entities.Usage;

public class CompanyUsage implements Serializable, Cacheable {
	
	private static final long serialVersionUID = 8084189529308101452L;

	private static final String KEY_PREFIX = "l#companyUsage_";
	
	public String companyId;
	public Usage usage;
		
	public CompanyUsage (String companyId, Usage usage) {
		
		this.companyId = companyId;
		this.usage = usage;
	}
	
	public String getId() {
		
		return companyId;
	}
	
	static public String getKeyPrefix() {
		
		return KEY_PREFIX;
	}
	
	static public CompanyUsage recreate(String companyId) {
		
		Usage usage = new Usage(companyId);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Calendar c = Calendar.getInstance();  
		c.add(Calendar.MONTH, -1);
		
		Query qq = new Query(EntityKind.MEDIA_LIBRARY_USAGE)
			.setFilter(
				CompositeFilterOperator.and(
						new FilterPredicate(Usage.COMPANY_ID, Query.FilterOperator.EQUAL, companyId),
						new FilterPredicate(Usage.CREATION_DATE, Query.FilterOperator.GREATER_THAN_OR_EQUAL, c.getTime())
				));
		
		Logger.getAnonymousLogger().warning("Retrieved from DB." + c.getTime().toString());

		List<Entity> children = datastore.prepare(qq).asList(FetchOptions.Builder.withDefaults().chunkSize(1000).prefetchSize(1000));
		
		Logger.getAnonymousLogger().warning("Companies found:" + children.size());

		for (Entity ce : children) {
			usage = new Usage(ce.getKey(), ce.getProperties());
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
		
		return new CompanyUsage(companyId, usage);
	}
	
}
