package com.risevision.storage.entities;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.risevision.storage.CacheUtils;
import com.risevision.storage.EntityKind;
import com.risevision.storage.Utils;

public class Usage extends AbstractEntity {

	private static final long serialVersionUID = 2077516329995928803L;

	private static final String KEY_PREFIX = "e#" + EntityKind.MEDIA_LIBRARY_USAGE.toLowerCase() + "_";
	
	// field names

	static public final String ID = "id";
	static public final String COMPANY_ID = "companyId";
	static public final String CREATION_DATE = "creationDate";
	static public final String CHANGE_DATE = "changeDate";
	static public final String AVERAGE_STORAGE = "averageStorage";
	static public final String LATEST_STORAGE = "latestStorage";
	static public final String TOTAL_BANDWIDTH = "totalBandwidth";

	// fields

	public String id;
	public String companyId;
	public Date creationDate;
	public Date changeDate;
	public Long averageStorage;
	public Long latestStorage;
	public Long totalBandwidth;
	
	public Usage(String companyId) {

		super();
		
		this.id = UUID.randomUUID().toString();
		this.companyId = companyId;
		this.averageStorage = 0L;
		this.latestStorage = 0L;
		this.totalBandwidth = 0L;
		
	}
	
	public Usage(String id, String companyId, Date creationDate, Long averageStorage, Long latestStorage, Long totalBandwidth) {

		super();
		
		this.id = id;
		this.companyId = companyId;
		this.creationDate = creationDate;
		this.averageStorage = averageStorage;
		this.latestStorage = latestStorage;
		this.totalBandwidth = totalBandwidth;
		
	}
	
	public Usage(Entity e) {
		super(e);
	}
	
	public Usage(Key k, Map<String, Object> p) {
		super(k, p);
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getEntityKind() {
		return EntityKind.MEDIA_LIBRARY_USAGE;
	}
	
	@Override
	public void getProperties(Map<String, Object> p)  {
		
		this.id = (String) p.get(ID);
		this.companyId = (String) p.get(COMPANY_ID);
		this.creationDate = (Date) p.get(CREATION_DATE);
		this.changeDate = (Date) p.get(CHANGE_DATE);
		this.averageStorage = p.get(AVERAGE_STORAGE) != null ? ((Long) p.get(AVERAGE_STORAGE)).longValue() : null;
		this.latestStorage = p.get(LATEST_STORAGE) != null ? ((Long) p.get(LATEST_STORAGE)).longValue() : null;
		this.totalBandwidth = p.get(TOTAL_BANDWIDTH) != null ? ((Long) p.get(TOTAL_BANDWIDTH)).longValue() : null;

	}

	@Override
	public void setProperties(Entity e) {

		e.setProperty(ID, id);
		e.setProperty(COMPANY_ID, companyId);
		e.setProperty(CREATION_DATE, creationDate);
		e.setUnindexedProperty(CHANGE_DATE, changeDate);
		e.setUnindexedProperty(AVERAGE_STORAGE, averageStorage.longValue());
		e.setUnindexedProperty(LATEST_STORAGE, latestStorage.longValue());
		e.setUnindexedProperty(TOTAL_BANDWIDTH, totalBandwidth.longValue());

	}
		
	@Override
	public void recordChange(Date changeDate) {
		
		this.changeDate = changeDate;
		if (this.creationDate == null) {
			this.creationDate = changeDate;
		}
		
	}
	
	@Override
	public Key put(DatastoreService datastore, Date changeDate) {

		Entity e = new Entity(getEntityKind(), getId());
	
		recordChange(changeDate);
		setProperties(e);
		
		key = datastore.put(e);
		
		CacheUtils.saveToCache(this);
		
		return key;
	}
	
	@Override
	public void delete(DatastoreService datastore) {
		
		if (key == null) {
			
			Logger.getAnonymousLogger().severe("Key is null, cannot delete!");
			return;
		}
		
		datastore.delete(key);
		CacheUtils.purgeFromCache(this.getClass(), getId());
	}
	
	public static Usage get(String id) {
		
		return CacheUtils.get(Usage.class, id, Utils.getEntityKey(EntityKind.MEDIA_LIBRARY_USAGE, id));
		
	}
	
	// ************************* Cacheable support *******************************
	

	static public String getKeyPrefix() {

		return KEY_PREFIX;
	}

	static public Usage recreate(String id) {

		return recreate(Utils.getEntityKey(EntityKind.MEDIA_LIBRARY_USAGE, id));
	}
	
	static public Usage recreate(Key key) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Usage result = null;

		try {
		
			Entity e = datastore.get(key);
			result = new Usage(e);
		
		} catch (EntityNotFoundException ex) {
			Logger.getAnonymousLogger().warning("Error: " + ex.getMessage());
			result = null;
		}
		
		return result;
	}

}
