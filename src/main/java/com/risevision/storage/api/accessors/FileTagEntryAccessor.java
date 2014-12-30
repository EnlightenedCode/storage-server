package com.risevision.storage.api.accessors;

import java.util.List;

import com.google.appengine.api.users.User;
import com.risevision.storage.Utils;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.TagDefinition;

public class FileTagEntryAccessor extends AbstractAccessor {
  private DatastoreService datastoreService;

  public FileTagEntryAccessor() {
    this.datastoreService = DatastoreService.getInstance();
  }

  public FileTagEntry put(String companyId, String objectId, String name, String type, List<String> values, User user) throws Exception {
    // Validate required fields
    if(Utils.isEmpty(companyId)) {
      throw new ValidationException("Company id is required");
    }

    if(Utils.isEmpty(objectId)) {
      throw new ValidationException("Object id is required");
    }

    if(Utils.isEmpty(name)) {
      throw new ValidationException("Tag name is required");
    }

    if(Utils.isEmpty(type)) {
      throw new ValidationException("Tag type is required");
    }

    type = type.toUpperCase();
    
    if(TagType.valueOf(type) == TagType.FREEFORM && values.size() != 1) {
      throw new ValidationException("Freeform tags must have exactly one value");
    }
    
    // Make sure all values are lower case.
    name = name.toLowerCase();
    Utils.changeValuesToLowerCase(values);
    
    // Verify if tag values exist in parent tag definition
    TagDefinition tagDef = new TagDefinitionAccessor().get(companyId, name, type);
    
    if(tagDef == null) {
      throw new ValidationException("Parent tag definition does not exist");
    }
    
    if(TagType.valueOf(type) == TagType.LOOKUP && (tagDef == null || !Utils.allItemsExist(values, tagDef.getValues()))) {
      throw new ValidationException("All tag values must exist in the parent tag definition");
    }

    // Also make sure name is lower case.
    FileTagEntry fileTagEntry = new FileTagEntry(companyId, objectId, name, type, values, user.getEmail());
    datastoreService.put(fileTagEntry);
    
    return fileTagEntry;
  }

  public FileTagEntry get(String id) throws Exception {
    return (FileTagEntry) datastoreService.get(new FileTagEntry(id));
  }

  public void delete(String id) throws Exception {
    datastoreService.delete(new FileTagEntry(id));
  }

  public PagedResult<FileTagEntry> list(String companyId, String search, Integer limit, String sort, String cursor) throws Exception {
    return datastoreService.list(FileTagEntry.class, limit, sort, cursor, mergeFilters(parseQuery(search), "companyId", companyId));
  }
}
