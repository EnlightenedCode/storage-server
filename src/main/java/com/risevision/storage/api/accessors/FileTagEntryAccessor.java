package com.risevision.storage.api.accessors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.risevision.storage.Utils;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.AutoTrashTag;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.TagDefinition;
import com.risevision.storage.entities.Timeline;

public class FileTagEntryAccessor extends AbstractAccessor {
  private DatastoreService datastoreService;
  private Gson gson;
  private DateFormat dateFormat;

  public FileTagEntryAccessor() {
    this.datastoreService = DatastoreService.getInstance();
    this.gson = new Gson();
    this.dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
  }

  public FileTagEntry put(String companyId, String objectId, String name, String type, List<String> values, User user) throws Exception {
    Timeline timeline = null;
    Date timelineEndDate = null;
    
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

    // Make sure all values are in the correct case.
    type = type.toUpperCase();
    name = name.toLowerCase();
    
    if(TagType.valueOf(type) == TagType.LOOKUP) {
      Utils.changeValuesToLowerCase(values);
    }
    
    if(TagType.valueOf(type) == TagType.FREEFORM && values.size() != 1) {
      throw new ValidationException("Freeform tags must have exactly one value");
    }
    
    if(TagType.valueOf(type) == TagType.TIMELINE) {
      try {
        timeline = gson.fromJson(values.get(0), Timeline.class);
        timelineEndDate = !Utils.isEmpty(timeline.getEndDate()) ? dateFormat.parse(timeline.getEndDate()) : null;
      }
      catch (JsonSyntaxException e) {
        throw new ValidationException("Timeline definition is not valid");
      }
      catch (ParseException e) {
        throw new ValidationException("Timeline end date is not valid");
      }
    }
    
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
    
    if(timelineEndDate != null) {
      AutoTrashTag autoTrashTag = new AutoTrashTag(companyId, objectId, timelineEndDate, user.getEmail());
      
      datastoreService.put(autoTrashTag);
    }
    
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
