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

public class FileLog extends AbstractEntity {

	private static final long serialVersionUID = -4556224590367615375L;

	private static final String KEY_PREFIX = "e#" + EntityKind.MEDIA_LIBRARY_FILE_LOG.toLowerCase() + "_";
	
	// field names

	static public final String ID = "id";
	static public final String COMPANY_ID = "companyId";
	static public final String FILE_ID = "fileId";
	static public final String IP = "ip";
	static public final String CREATION_DATE = "creationDate";
	static public final String CHANGE_DATE = "changeDate";
	static public final String FILE_SIZE = "fileSize";
	static public final String TOTAL_REQUESTS = "totalRequests";
	static public final String CACHED_REQUESTS = "cachedRequests";
	static public final String TOTAL_BANDWIDTH = "totalBandwidth";
	
	// fields

	public String id;
	public String companyId;
	public String fileId;
	public String ip;
	public Date creationDate;
	public Date changeDate;
	public Long fileSize;
	public Integer totalRequests;
	public Integer cachedRequests;
	public Long totalBandwidth;
	
	private boolean changed = false;
	
	public FileLog(String companyId, String fileId, String ip) {
		super();
		
		this.id = UUID.randomUUID().toString();
		this.companyId = companyId;
		this.fileId = fileId;
		this.ip = ip;
		totalRequests = 0;
		cachedRequests = 0;
		totalBandwidth = 0L;
		
	}

	public FileLog(String id, String companyId, String fileId, String ip, Long fileSize, Integer totalRequests, Integer cachedRequests,
			Long totalBandwidth) {

		super();
		
		this.id = id;
		this.companyId = companyId;
		this.fileId = fileId;
		this.ip = ip;
		this.fileSize = fileSize;
		this.totalRequests = totalRequests;
		this.cachedRequests = cachedRequests;
		this.totalBandwidth = totalBandwidth;

	}
	
	public FileLog(Entity e) {
		super(e);
	}
	
	public FileLog(Key k, Map<String, Object> p) {
		super(k, p);
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getEntityKind() {
		return EntityKind.MEDIA_LIBRARY_FILE_LOG;
	}
	
	@Override
	public void getProperties(Map<String, Object> p)  {
		
		this.id = (String) p.get(ID);
		this.companyId = (String) p.get(COMPANY_ID);
		this.fileId = (String) p.get(FILE_ID);
		this.ip = (String) p.get(IP);
		this.creationDate = (Date) p.get(CREATION_DATE);
		this.changeDate = (Date) p.get(CHANGE_DATE);
		this.fileSize = p.get(FILE_SIZE) != null ? ((Long) p.get(FILE_SIZE)).longValue() : null;
		this.totalRequests = p.get(TOTAL_REQUESTS) != null ? ((Long) p.get(TOTAL_REQUESTS)).intValue() : null;
		this.cachedRequests = p.get(CACHED_REQUESTS) != null ? ((Long) p.get(CACHED_REQUESTS)).intValue() : null;
		this.totalBandwidth = p.get(TOTAL_BANDWIDTH) != null ? ((Long) p.get(TOTAL_BANDWIDTH)).longValue() : null;
		
	}

	@Override
	public void setProperties(Entity e) {

		e.setProperty(ID, id);
		e.setProperty(COMPANY_ID, companyId);
		e.setProperty(FILE_ID, fileId);
		e.setProperty(IP, ip);
		e.setProperty(CREATION_DATE, creationDate);

		e.setUnindexedProperty(CHANGE_DATE, changeDate);
		e.setUnindexedProperty(FILE_SIZE, fileSize);
		e.setUnindexedProperty(TOTAL_REQUESTS, totalRequests);
		e.setUnindexedProperty(CACHED_REQUESTS, cachedRequests);
		e.setUnindexedProperty(TOTAL_BANDWIDTH, totalBandwidth);

	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged() {
		changed = true;
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
		
		changed = false;
		
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
	
	public static FileLog get(String id) {
		
		return CacheUtils.get(FileLog.class, id, Utils.getEntityKey(EntityKind.MEDIA_LIBRARY_FILE_LOG, id));
		
	}
	
	// ************************* Cacheable support *******************************
	

	static public String getKeyPrefix() {

		return KEY_PREFIX;
	}

	static public FileLog recreate(String id) {

		return recreate(Utils.getEntityKey(EntityKind.MEDIA_LIBRARY_FILE_LOG, id));
	}
	
	static public FileLog recreate(Key key) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		FileLog result = null;

		try {
		
			Entity e = datastore.get(key);
			result = new FileLog(e);
		
		} catch (EntityNotFoundException ex) {
			Logger.getAnonymousLogger().warning("Error: " + ex.getMessage());
			result = null;
		}
		
		return result;
	}

}
