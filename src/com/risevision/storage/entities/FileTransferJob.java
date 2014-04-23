package com.risevision.storage.entities;

import java.util.Date;
import java.util.List;
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

public class FileTransferJob extends AbstractEntity {

	private static final long serialVersionUID = -5014493447730572806L;

	private static final String KEY_PREFIX = "e#" + EntityKind.STORAGE_FILE_TRANSFER_JOB.toLowerCase() + "_";
	
	// field names
	static public final String ID = "id";
	static public final String JOB_ID = "jobId";
	static public final String JOB_TYPE = "jobType";
	static public final String JOB_STATUS = "jobStatus";
	static public final String FILE_NAMES = "fileNames";
	static public final String CREATION_DATE = "creationDate";
	static public final String CHANGE_DATE = "changeDate";
	
	// fields
	public String id;
	public String jobId;
	public Integer jobType;
	public Integer jobStatus;
	public List<String> fileNames;
	public Date creationDate;
	public Date changeDate;
	
	private boolean changed = false;
	
	public FileTransferJob(String jobId, int jobType, List<String> fileNames) {
		super();
		
		this.id = UUID.randomUUID().toString();
		this.jobId = jobId;
		this.jobType = jobType;
		this.fileNames = fileNames;

		jobStatus = 0;
		
	}

	public FileTransferJob(String id, String jobId, int jobType, int jobStatus, List<String> fileNames) {

		super();
		
		this.id = id;
		this.jobId = jobId;
		this.jobType = jobType;
		this.jobStatus = jobStatus;
		this.fileNames = fileNames;

	}
	
	public FileTransferJob(Entity e) {
		super(e);
	}
	
	public FileTransferJob(Key k, Map<String, Object> p) {
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
	@SuppressWarnings("unchecked")
	public void getProperties(Map<String, Object> p)  {
		
		this.id = (String) p.get(ID);
		this.jobId = (String) p.get(JOB_ID);
		this.jobType = p.get(JOB_TYPE) != null ? ((Long) p.get(JOB_TYPE)).intValue() : null;
		this.jobStatus = p.get(JOB_STATUS) != null ? ((Long) p.get(JOB_STATUS)).intValue() : null;
		this.fileNames = (List<String>) p.get(FILE_NAMES);
		this.creationDate = (Date) p.get(CREATION_DATE);
		this.changeDate = (Date) p.get(CHANGE_DATE);		
	}

	@Override
	public void setProperties(Entity e) {

		e.setProperty(ID, id);
		e.setProperty(JOB_ID, jobId);
		e.setProperty(JOB_TYPE, jobType);
		e.setProperty(JOB_STATUS, jobStatus);
		
		e.setUnindexedProperty(FILE_NAMES, fileNames);
		e.setUnindexedProperty(CREATION_DATE, creationDate);
		e.setUnindexedProperty(CHANGE_DATE, changeDate);

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
	
	public static FileTransferJob get(String id) {
		
		return CacheUtils.get(FileTransferJob.class, id, Utils.getEntityKey(EntityKind.STORAGE_FILE_TRANSFER_JOB, id));
		
	}
	
	// ************************* Cacheable support *******************************
	

	static public String getKeyPrefix() {

		return KEY_PREFIX;
	}

	static public FileTransferJob recreate(String id) {

		return recreate(Utils.getEntityKey(EntityKind.STORAGE_FILE_TRANSFER_JOB, id));
	}
	
	static public FileTransferJob recreate(Key key) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		FileTransferJob result = null;

		try {
		
			Entity e = datastore.get(key);
			result = new FileTransferJob(e);
		
		} catch (EntityNotFoundException ex) {
			Logger.getAnonymousLogger().warning("Error: " + ex.getMessage());
			result = null;
		}
		
		return result;
	}

}
