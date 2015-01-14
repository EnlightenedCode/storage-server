package com.risevision.storage.api.accessors;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.users.User;
import com.risevision.storage.Utils;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.TagDefinition;

public class TagDefinitionAccessor extends AbstractAccessor {
  private DatastoreService datastoreService;

  public TagDefinitionAccessor() {
    datastoreService = DatastoreService.getInstance();
  }

  public TagDefinition put(String companyId, String type, String name, List<String> values, User user) throws Exception {
    // Validate required fields
    if(Utils.isEmpty(companyId)) {
      throw new ValidationException("Company id is required");
    }

    if(Utils.isEmpty(name)) {
      throw new ValidationException("Tag name is required");
    }

    name = name.toLowerCase();

    if(Utils.isEmpty(type)) {
      throw new ValidationException("Tag type is required");
    }

    type = type.toUpperCase();
    
    try {
      TagType.valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Tag type is invalid");
    }

    if(TagType.valueOf(type) == TagType.FREEFORM && values != null) {
      throw new ValidationException("Freeform tags must not have predefined values");
    }
    
    if(TagType.valueOf(type) == TagType.LOOKUP && values == null) {
      throw new ValidationException("Lookup tags must have predefined values");
    }

    Utils.changeValuesToLowerCase(values);

    TagDefinition tagDefinition = new TagDefinition(companyId, type, name, values, user.getEmail());
    datastoreService.put(tagDefinition);

    return tagDefinition;
  }
  
  public TagDefinition get(String id) throws Exception {
    return (TagDefinition) datastoreService.get(new TagDefinition(id));
  }
  
  public TagDefinition get(String companyId, String type, String name) throws Exception {
    PagedResult<TagDefinition> result = datastoreService.list(TagDefinition.class, null, null, null, mergeFilters(new ArrayList<Condition>(), "companyId", companyId, "name", name));
    
    for(TagDefinition tagDefinition : result.getList()) {
      if(tagDefinition.getType().equals(type)) {
        return tagDefinition;
      }
    }
    
    return null;
  }

  public void delete(String id) throws Exception {
    datastoreService.delete(new TagDefinition(id));
  }

  public PagedResult<TagDefinition> list(String companyId, String search, Integer limit, String sort, String cursor) throws Exception {
    return datastoreService.list(TagDefinition.class, limit, sort, cursor, mergeFilters(parseQuery(search), "companyId", companyId));
  }
}
