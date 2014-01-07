package com.risevision.medialibrary.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class CacheUtils {
	
	static public <T extends Serializable & Cacheable> void saveToCache(T value) {


		try {
			
			MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
			cache.put(getKeyPrefix(value.getClass()) + value.getId(), value);

		} catch (Exception e) {
			Logger.getAnonymousLogger().warning("Cache write error: " + e.toString() + " " + e.getMessage());
			Utils.logStackTrace(e, Logger.getAnonymousLogger());
		}
	}
	
	@SuppressWarnings("unchecked")
	static public <T extends Serializable & Cacheable> T loadFromCache(Class<T> c, String id) {
		
		T result = null;

		try {
			
			MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
			result = (T) cache.get(getKeyPrefix(c) + id);

		} catch (Exception e) {
			result = null;
		}
		
		return result;
	}
		
	static public <T extends Serializable & Cacheable> void purgeFromCache(Class<T> c, String id) {
		
		try {

			MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
			cache.delete(getKeyPrefix(c) + id);
			
			Logger.getAnonymousLogger().info("Instance of " + c.getName() + " (ID: " + getKeyPrefix(c) + id + ") purged from cache.");

		} catch (Exception e) {
			Logger.getAnonymousLogger().warning("Cache purge error: " + e.toString());
			Utils.logStackTrace(e, Logger.getAnonymousLogger());
		}
	}
	
	static public <T extends Serializable & Cacheable> T get(Class<T> c, String id) {
		
		T result = null;

		try {
			result = loadFromCache(c, id);

			if (result == null) {
				
				Logger.getAnonymousLogger().warning("Instance of " + c.getName() + " (ID: " + getKeyPrefix(c) + id + ") not found in the cache, retrieving from data store.");
				
				result = recreate(c, id);
				
				if (result != null) {
				
					Logger.getAnonymousLogger().warning("Re-caching.");
					saveToCache(result);
				
				} else {
					Logger.getAnonymousLogger().warning("Instance of " + c.getName() + " (ID: " + getKeyPrefix(c) + id + ") could not be recreated. Perhaps it doesn't exist?");
				}
			}

		} catch (Exception e) {
			Logger.getAnonymousLogger().warning("Cache get error: " + e.toString());
			Utils.logStackTrace(e, Logger.getAnonymousLogger());
			result = null;
		}

		return result;
	}
	
	static public <T extends Serializable & Cacheable> T get(Class<T> c, String id, Key key) {
		
		T result = null;

		try {
			result = loadFromCache(c, id);

			if (result == null) {
				
				Logger.getAnonymousLogger().warning("Instance of " + c.getName() + " (ID: " + getKeyPrefix(c) + id + ") not found in the cache, retrieving from data store.");
				
				result = recreate(c, key);
				
				if (result != null) {
				
					Logger.getAnonymousLogger().warning("Re-caching.");
					saveToCache(result);
				
				} else {
					Logger.getAnonymousLogger().warning("Instance of " + c.getName() + " (ID: " + getKeyPrefix(c) + id + ") could not be recreated. Perhaps it doesn't exist?");
				}
			}

		} catch (Exception e) {
			Logger.getAnonymousLogger().warning("Cache get error: " + e.toString());
			Utils.logStackTrace(e, Logger.getAnonymousLogger());
			result = null;
		}

		return result;
	}
	
	static private <T> String getKeyPrefix(Class<T> c) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		Class<?>[] params = {};
		Object[] values = {};
		
		return (String)c.getMethod("getKeyPrefix", params).invoke(null, values);
	}
	
	@SuppressWarnings("unchecked")
	static private <T> T recreate(Class<T> c, String id) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		Class<?>[] params = {String.class};
		Object[] values = {id};
		
		return (T)c.getMethod("recreate", params).invoke(null, values);
	}
	
	@SuppressWarnings("unchecked")
	static private <T> T recreate(Class<T> c, Key key) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		Class<?>[] params = {Key.class};
		Object[] values = {key};
		
		return (T)c.getMethod("recreate", params).invoke(null, values);
	}
		
	static public void clearEntireCache(){
		
		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		cache.clearAll();
	}

}
