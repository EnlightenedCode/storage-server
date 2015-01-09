package com.risevision.storage.api.accessors;

import java.util.Date;

import com.google.appengine.api.users.User;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.AutoTrashTag;

public class AutoTrashTagAccessor extends AbstractAccessor {
  private DatastoreService datastoreService;

  public AutoTrashTagAccessor() {
    datastoreService = DatastoreService.getInstance();
  }

  public AutoTrashTag put(String companyId, String objectId, Date expiration, User user) {
    AutoTrashTag autoTrashTag = new AutoTrashTag(companyId, objectId, expiration, user.getEmail());
    
    return (AutoTrashTag) datastoreService.put(autoTrashTag);
  }
  
  public AutoTrashTag get(String id) {
    return (AutoTrashTag) datastoreService.get(new AutoTrashTag(id));
  }
  
  public void delete(String id) {
    datastoreService.delete(new AutoTrashTag(id));
  }
  
  public AutoTrashTag getByObjectId(String objectId) {
    PagedResult<AutoTrashTag> list = datastoreService.list(AutoTrashTag.class, null, null, null, parseQuery("objectId:" + objectId));
    
    if(list.getList().size() > 0) {
      return list.getList().get(0);
    }
    else {
      return null;
    }
  }

  public PagedResult<AutoTrashTag> list(String companyId, String search, Integer limit, String sort, String cursor) {
    return datastoreService.list(AutoTrashTag.class, limit, sort, cursor, mergeFilters(parseQuery(search), "companyId", companyId));
  }

  public PagedResult<AutoTrashTag> listExpired(Date expiration, Integer limit, String sort, String cursor) {
    return datastoreService.list(AutoTrashTag.class, limit, sort, cursor, new Condition("expiration <", expiration));
  }
}
