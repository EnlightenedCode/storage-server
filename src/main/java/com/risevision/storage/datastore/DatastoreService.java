package com.risevision.storage.datastore;

/**
 * Created by rodrigopavezi on 12/9/14.
 */

import static com.risevision.storage.datastore.OfyService.ofy;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import com.risevision.storage.Utils;

public class DatastoreService {
  public static class PagedResult<T> {
    List<T> list;
    String cursor;
    
    public PagedResult(List<T> list, String cursor) {
      this.list = list;
      this.cursor = cursor;
    }
    
    public List<T> getList() {
      return list;
    }
    
    public String getCursor() {
      return cursor;
    }
  }
  
  private static DatastoreService instance;
  private DatastoreService() {}

  public static DatastoreService getInstance() {
    try {
      if (instance == null) {
        instance = new DatastoreService();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return instance;
   }

  public Object put(Object entity) {
    ofy().save().entity(entity).now();
    
    return entity;
  }
  
  public void put(List<?> entities) {
    ofy().save().entities(entities).now();
  }

  public Object get(Object entity) {
    return ofy().load().entity(entity).now();
  }

  public Object delete(Object entity) {
    ofy().delete().entity(entity).now();
    
    return entity;
  }

  public <T> PagedResult<T> list(Class<T> clazz, Integer limit, String sort, String cursor, Object... conditions) {
    Query<T> query = ofy().load().type(clazz);
    List<T> result = new ArrayList<T>();
    
    if(limit != null) {
      query = query.limit(limit);
    }
    
    if(!Utils.isEmpty(cursor)) {
      query = query.startAt(Cursor.fromWebSafeString(cursor));
    }
    
    if(!Utils.isEmpty(sort)) {
      query.order(sort);
    }
    
    for(int i = 0; i < conditions.length - 1; i += 2) {
      query = query.filter((String) conditions[i], conditions[i + 1]);
    }
    
    QueryResultIterator<T> iterator = query.iterator();
    
    while(iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    if(limit == null || result.size() < limit) {
      return new PagedResult<T>(result, null);
    }
    else {
      return new PagedResult<T>(result, iterator.getCursor().toWebSafeString());
    }
  }
}
