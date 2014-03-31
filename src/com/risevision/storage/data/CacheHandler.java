package com.risevision.storage.data;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.InvalidValueException;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

public class CacheHandler {
	public static final Logger log = Logger.getLogger(CacheHandler.class.getName());

	private static CacheHandler instance;
	private Cache cache;

	private CacheHandler() {
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			// Log stuff
			log.log(Level.WARNING, "Error in creating the Cache");
		}
	}

	public static synchronized CacheHandler getInstance() {
		if (instance == null) {
			instance = new CacheHandler();
		}
		return instance;
	}

	public Object findInCache(String word) {
		if (cache.containsKey(word)) {
			try {
				return cache.get(word);
			}
			catch (InvalidValueException e) {
				// Datastore cache version out of date (serialization error)
				return null;
			}
		} else {
			return null;
		}
	}

	public void putInCache(String word, Object definition) {
		cache.put(word, definition);
	}
	
	public void removeFromCache(String word) {
		cache.remove(word);
	}
}
