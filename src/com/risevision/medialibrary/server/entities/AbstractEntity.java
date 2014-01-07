package com.risevision.medialibrary.server.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.risevision.medialibrary.server.CacheUtils;
import com.risevision.medialibrary.server.Cacheable;
import com.risevision.medialibrary.server.Globals;

public abstract class AbstractEntity implements Serializable, Cacheable {
	
	private static final long serialVersionUID = 1L;

	public Key key;
	
	public AbstractEntity() {
		
		this.key = null;
	}
	
	public AbstractEntity(Entity e) {
		
		this.key = e.getKey();
		
		if (!e.getKind().equals(getEntityKind())) {
			
			Logger.getAnonymousLogger().severe("Wrong entity kind: " + e.getKind() + ". Expected " + getEntityKind());
		
		} else {
		
			getProperties(e);
		
		}
	}
	
	public AbstractEntity(Key k, Map<String, Object> p) {
		
		this.key = k;

		if (!k.getKind().equals(getEntityKind())) {
			
			Logger.getAnonymousLogger().severe("Wrong entity kind: " + k.getKind() + ". Expected " + getEntityKind());
		
		} else {
		
			getProperties(p);
		
		}
	}
	
//	public AbstractEntity(Form form) {
//		
//		this.key = null;
//		setDefaults();
//		updateFromForm(form);
//	}
	
	public void getProperties(Entity e) {
		
		Map<String, Object> p = e.getProperties();
		getProperties(p);
	}
	
	public abstract void getProperties(Map<String, Object> p);
	
	public abstract void setProperties(Entity e);
	
	public abstract String getId();
	
	public abstract String getEntityKind();
	
	public abstract void recordChange(Date changeDate);
	
	public void setDefaults() {
		
	}
	
//	public void updateFromForm(Form form) {
//		
//	}
	
	public Key put(DatastoreService datastore, Date changeDate) {
		
		return put(datastore, changeDate, true); // reread keys by default
	}
	
	public Key put(DatastoreService datastore, Date changeDate, Boolean reReadKey) {
		
		if (reReadKey) {

			AbstractEntity latest = CacheUtils.get(this.getClass(), this.getId());

			if (latest == null) {
				Logger.getAnonymousLogger().severe("Entity does not exist. Perhaps it has been deleted?");
				return null;
			}

			key = latest.key; // IMPORTANT: re-read key in case the entity was moved in hierarchy
		}
		
		if (key == null) {
			Logger.getAnonymousLogger().severe("Key is null?!");
			return null;
		}
		
		Entity e = new Entity(key);
		
		recordChange(changeDate);
		setProperties(e);
		
		key = datastore.put(e);
		
		Logger.getAnonymousLogger().info("Put: " + e.toString());
		
		CacheUtils.saveToCache(this);
		
		return key;
	}
	
	public Key put(DatastoreService datastore, Key parentKey, Date changeDate) {
		
		if (parentKey == null) {
			Logger.getAnonymousLogger().severe("ParentKey is null!");
			return null;
		}
		
		Entity e = new Entity(getEntityKind(), getId(), parentKey);
		
		recordChange(changeDate);
		setProperties(e);
		
		key = datastore.put(e);
		
		Logger.getAnonymousLogger().info("Put: " + e.toString());
		
		CacheUtils.saveToCache(this);
		
		return key;
	}
	
	public Key put(DatastoreService datastore) {
		
		return put(datastore, new Date());
		
	}
	
	public Key put(DatastoreService datastore, Key parentKey) {
		
		return put(datastore, parentKey, new Date());
		
	}
	
	public Key put(Date changeDate) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return put(datastore);
		
	}
	
	public Key put(Key parentKey, Date changeDate) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return put(datastore, parentKey, changeDate);
		
	}
	
	public Key put() {
	
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return put(datastore);
		
	}
	
	public Key put(Key parentKey) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return put(datastore, parentKey);
		
	}
	
	public void delete(DatastoreService datastore) {

		if (key == null) {
		
			Logger.getAnonymousLogger().severe("Key is null, cannot delete!");
			return;
		}
		
		// move the entity and all its children to the deleted namespace
		
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		
		Query q = new Query().setAncestor(key);
		List<Entity> children = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults().chunkSize(100).prefetchSize(100));
		for (Entity ce : children) {
			
			if (ce.hasProperty(AbstractCompanyEntity.CHANGE_DATE)) {
				ce.setUnindexedProperty(AbstractCompanyEntity.CHANGE_DATE, new Date());
			}
				
			String currentNamespace = NamespaceManager.get();
			try {
				NamespaceManager.set(Globals.DELETED_NAMESPACE);
				
				Key dkey = KeyFactory.createKey(ce.getKind(), KeyFactory.keyToString(ce.getKey()));
				Entity de = new Entity(dkey);
				de.setPropertiesFrom(ce);
				datastore.put(de);

			} finally {
				NamespaceManager.set(currentNamespace);
			}	
			
			datastore.delete(ce.getKey());
			Logger.getAnonymousLogger().info("Deleted: " + ce.toString());
			
			// make sure the entity is purged from the cache
			cache.delete("e#" + ce.getKind().toLowerCase() + "_" + ce.getKey().getName());
			Logger.getAnonymousLogger().info("e#" + ce.getKind().toLowerCase() + "_" + ce.getKey().getName() + " purged from cache.");
			
		}
		
		CacheUtils.purgeFromCache(this.getClass(), getId());
	}
	
	public void delete() {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		delete(datastore);
		
	}
	
//	protected String updateFromForm(Form form, String attributeName, String prevValue) {
//		
//		if (form.getNames().contains(attributeName)) {
//			String newValue = form.getFirstValue(attributeName);
//			return newValue != null ? newValue : ""; 
//		} else {
//			return prevValue;
//		}
//	}
		
//	protected Boolean updateFromForm(Form form, String attributeName, Boolean prevValue) {
//		
//		if (form.getNames().contains(attributeName)) {
//			String newValue = form.getFirstValue(attributeName);
//			return (newValue != null && !newValue.isEmpty()) ? newValue.equals(Global.TRUE) : prevValue; 
//		} else {
//			return prevValue;
//		}
//	}
	
//	protected Integer updateFromForm(Form form, String attributeName, Integer prevValue) {
//		
//		if (form.getNames().contains(attributeName)) {
//			
//			String newValue = form.getFirstValue(attributeName);
//			
//			if (newValue == null || newValue.isEmpty())
//				return prevValue;
//			
//			try {
//    			int i = Integer.parseInt(newValue);
//    			return i;
//    		} catch (NumberFormatException e) {
//    			return prevValue;
//    		}
//			
//		} else {
//			return prevValue;
//		}
//	}
	
//	protected Date updateFromForm(Form form, String attributeName, Date prevValue) {
//
//		if (form.getNames().contains(attributeName)) {
//
//			String newValue = form.getFirstValue(attributeName);
//
//			if (newValue == null || newValue.isEmpty())
//				//return prevValue;
//				return null;
//
//			try {
//				SimpleDateFormat formatter = new SimpleDateFormat(Format.DATE_RFC822);  
//				Date d = (Date)formatter.parse(newValue);
//				return d;
//			} catch (ParseException e) {
//				return prevValue;
//			}
//
//		} else {
//			return prevValue;
//		}
//	}
	
//	protected List<String> updateFromForm(Form form, String attributeName, List<String> prevValue) {
//
//		if (form.getNames().contains(attributeName)) {
//			
//			String tmp = form.getFirstValue(attributeName);
//			List<String> newValue = new ArrayList<String>();
//			
//			if (tmp != null) {
//
//				String[] ss = tmp.split(",");
//				for (int i = 0; i < ss.length; i++) {
//					if(!ss[i].isEmpty()) {
//						newValue.add(ss[i].trim());
//					}
//				}
//			} 
//			
//			return newValue;
//			
//		} else {
//			return prevValue != null ? prevValue : new ArrayList<String>();
//		}
//	}
//	
//	public Element toXML(Document document) {
//		
//		return null;
//	}
	
}
