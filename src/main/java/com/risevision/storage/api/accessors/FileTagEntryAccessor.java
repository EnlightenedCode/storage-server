package com.risevision.storage.api.accessors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.risevision.storage.Utils;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.AutoTrashTag;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.StorageEntity;
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

  public FileTagEntry put(String companyId, String objectId, String type, String name, List<String> values, User user) throws Exception {
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
    // Also make sure name is lower case.
    name = name.toLowerCase();
    
    try {
      TagType.valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Tag type is invalid");
    }
    
    // If values is null, remove the FileTagEntry
    if(values == null) {
      return delete(new FileTagEntry(companyId, objectId, type, name, values, user.getEmail()).getId());
    }
    
    if(TagType.valueOf(type) == TagType.LOOKUP) {
      // Convert to lower case an remove duplicates
      Utils.changeValuesToLowerCase(values);
      
      if(values != null) {
        values = new ArrayList<String>(new LinkedHashSet<String>(values));
      }
    }
    
    if(TagType.valueOf(type) == TagType.FREEFORM && values.size() != 1) {
      throw new ValidationException("Freeform tags must have exactly one value");
    }
    
    if(TagType.valueOf(type) == TagType.TIMELINE) {
      // Verify Timeline definition is valid
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
    else {
      // Verify if tag values exist in parent tag definition
      TagDefinition tagDef = new TagDefinitionAccessor().get(companyId, type, name);
      
      if(tagDef == null) {
        throw new ValidationException("Parent tag definition does not exist");
      }
      
      if(TagType.valueOf(type) == TagType.LOOKUP && (tagDef == null || !Utils.allItemsExist(values, tagDef.getValues()))) {
        throw new ValidationException("All tag values must exist in the parent tag definition");
      }
    }
    
    FileTagEntry fileTagEntry = new FileTagEntry(companyId, objectId, type, name, values, user.getEmail());
    
    datastoreService.put(fileTagEntry);
    
    if(TagType.valueOf(type) == TagType.TIMELINE) {
      AutoTrashTag autoTrashTag = new AutoTrashTag(companyId, objectId, timelineEndDate, user.getEmail());
      
      if(timelineEndDate != null && timeline.isTrash()) {
        datastoreService.put(autoTrashTag);
      }
      else {
        datastoreService.delete(autoTrashTag);
      }
    }
    
    return fileTagEntry;
  }

  public FileTagEntry get(String id) throws Exception {
    return (FileTagEntry) datastoreService.get(new FileTagEntry(id));
  }
  
  public FileTagEntry delete(String id) throws Exception {
    FileTagEntry fileTagEntry = (FileTagEntry) datastoreService.get(new FileTagEntry(id));
    
    if(fileTagEntry != null) {
      // If the tag type is Timeline and is marked as Trash after expiration, tries to get an AutoTrashTag.
      // If it exists, it is deleted (only one can exist per companyId-objectId, so no extra checks are needed)
      if(TagType.valueOf(fileTagEntry.getType()) == TagType.TIMELINE) {
        Timeline timeline = gson.fromJson(fileTagEntry.getValues().get(0), Timeline.class);
        
        if(timeline.isTrash()) {
          datastoreService.delete(new AutoTrashTag(fileTagEntry.getCompanyId(), fileTagEntry.getObjectId(), null, null));
        }
      }
      
      datastoreService.delete(fileTagEntry);
    }
    
    return fileTagEntry;
  }
  
  public void updateObjectId(String companyId, Collection<String> objectIds, Collection<String> newObjectIds) {
    List<FileTagEntry> updated = new ArrayList<FileTagEntry>();
    Iterator<String> itOld = objectIds.iterator();
    Iterator<String> itNew = newObjectIds.iterator();
    
    for(int i = 0; i < objectIds.size(); i++) {
      String objectId = itOld.next();
      String newObjectId = itNew.next();
      
      PagedResult<FileTagEntry> result = datastoreService.list(FileTagEntry.class, null, null, null, "companyId", companyId, "objectId", objectId);
      
      for(FileTagEntry entry : result.getList()) {
        entry.setObjectId(newObjectId);
        updated.add(entry);
      }
    }
    
    datastoreService.put((List<?>) updated);
  }
  
  public void deleteTagsByObjectId(String companyId, Collection<String> objectIds) {
    List<FileTagEntry> deleted = new ArrayList<FileTagEntry>();
    
    for(String objectId : objectIds) {
      PagedResult<FileTagEntry> result = datastoreService.list(FileTagEntry.class, null, null, null, "companyId", companyId, "objectId", objectId);
      
      for(FileTagEntry entry : result.getList()) {
        deleted.add(entry);
      }
    }
    
    datastoreService.delete((List<?>) deleted);
  }

  public PagedResult<FileTagEntry> list(String search, Integer limit, String sort, String cursor) throws Exception {
    return datastoreService.list(FileTagEntry.class, limit, sort, cursor, mergeFilters(parseQuery(search)));
  }

  public PagedResult<FileTagEntry> list(String companyId, String search, Integer limit, String sort, String cursor) throws Exception {
    return datastoreService.list(FileTagEntry.class, limit, sort, cursor, mergeFilters(parseQuery(search), "companyId", companyId));
  }
  
  public List<StorageEntity> listFilesByTags(String companyId, List<String> tags, boolean returnTags) throws Exception {
    // Finds all TagEntries with any of the given tag names (values are processed in the next stage)
    PagedResult<FileTagEntry> entries;
    Map<String, List<String>> tagsMap = buildTagsMap(tags);
    Map<String, StorageEntity> filesMap = new HashMap<String, StorageEntity>();
    Map<String, String> matchedFilesMap = new HashMap<String, String>();
    List<StorageEntity> result = new ArrayList<StorageEntity>();
    
    // If tags are not being returned, filter the result set by matched tagname using Datastore API
    if(!returnTags) {
      entries = datastoreService.list(FileTagEntry.class, null, null, null, mergeFilters("companyId", companyId, "name", buildTagNamesList(tags)));
    }
    else {
      entries = datastoreService.list(FileTagEntry.class, null, null, null, "companyId", companyId);
    }

    for(FileTagEntry entry : entries.getList()) {
      // Since we don't know in advance which files will match one of the given criteria tags in a following iteration, 
      // all files are added and later filtered based on the matchedFiles map. 
      StorageEntity stgEnt = filesMap.get(entry.getObjectId());
      
      if(stgEnt == null) {
        filesMap.put(entry.getObjectId(), stgEnt = new StorageEntity(entry.getObjectId()));
      }
      
      if(returnTags) {
        stgEnt.getTags().add(entry);
      }
      
      // Gets the accepted values for the matching tag name
      List<String> filteredValues = tagsMap.get(entry.getName());
      
      if(filteredValues != null) {
        // If only tag name was provided, any objectId with that tag name is considered a match
        if(filteredValues.size() == 0) {
          matchedFilesMap.put(entry.getObjectId(), entry.getObjectId());
        }
        else {
          for(String value : entry.getValues()) {
            // If any of the values is found, the file is considered a match
            if(filteredValues.contains(value)) {
              matchedFilesMap.put(entry.getObjectId(), entry.getObjectId());
            }
          }
        }
      }
    }
    
    // Only return files matching the given criteria
    for(String objectId : matchedFilesMap.keySet()) {
      result.add(filesMap.get(objectId));
    }
    
    return result;
  }
  
  /**
   * Builds a list of unique tag names
   * 
   * @param tags The list of tag:value pairs to process
   * @return The resulting list
   */
  protected List<String> buildTagNamesList(List<String> tags) {
    Set<String> names = new HashSet<String>();
    
    // Tag filters have the form tagName:expectedValue
    for(String tag : tags) {
      names.add(getFilterName(tag));
    }
    
    return new ArrayList<String>(names);
  }

  /**
   * Transforms a list of filters of the form [ tag1:val1, tag1:val2, tag2:val3 ] into a map of the form { tag1: [val1, val2], tag2: [val3] }
   * 
   * @param tags The list of tag:value pairs to process
   * @return The resulting map
   */
  protected Map<String, List<String>> buildTagsMap(List<String> tags) {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    
    for(String tag : tags) {
      String name = getFilterName(tag);
      String value = getFilterValue(tag);
      
      if(!map.containsKey(name)) {
        map.put(name, new ArrayList<String>());
      }
      
      if(!value.trim().equals("")) {
        map.get(name).add(value);
      }
    }
    
    return map;
  }
}
