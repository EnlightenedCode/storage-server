package com.risevision.storage.api.accessors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.risevision.storage.Globals;
import com.risevision.storage.Utils;
import com.risevision.storage.api.exception.ValidationException;
import com.risevision.storage.datastore.DatastoreService;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.Tag;
import com.risevision.storage.entities.TagDefinition;
import com.risevision.storage.entities.TaggedStorageObject;
import com.risevision.storage.entities.Timeline;

public class TaggedStorageObjectAccessor extends AbstractAccessor {
  private DatastoreService datastoreService;
  private Gson gson;
  private DateFormat dateFormat;

  public TaggedStorageObjectAccessor() {
    this.datastoreService = DatastoreService.getInstance();
    this.gson = new Gson();
    this.dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
  }

  public TaggedStorageObject put(String companyId, String objectId, List<Tag> tags, String timeline, User user) throws Exception {
    // Use sets to avoid duplicates
    Set<String> lookupNames = new HashSet<String>();
    Set<String> lookupTags = new HashSet<String>();
    Set<String> freeformNames = new HashSet<String>();
    Set<String> freeformTags = new HashSet<String>();
    Timeline tl = null;
    Date autoTrashDate = null;
    
    // Validate required fields
    if(Utils.isEmpty(companyId)) {
      throw new ValidationException("Company id is required");
    }

    if(Utils.isEmpty(objectId)) {
      throw new ValidationException("Object id is required");
    }
    
    // Get the tag definitions to validate lookup and freeform tags
    Map<String, TagDefinition> tagDefs = getTagDefinitions(companyId);
    
    for(Tag tag : tags) {
      if(Utils.isEmpty(tag.getName())) {
        throw new ValidationException("Tag name is required");
      }

      if(Utils.isEmpty(tag.getType())) {
        throw new ValidationException("Tag type is required: " + tag.getName());
      }

      if(Utils.isEmpty(tag.getValue())) {
        throw new ValidationException("Tag value is required: " + tag.getName() + " " + tag.getType());
      }
      
      // Make sure type, name and value are in the correct case
      tag.setType(tag.getType().toUpperCase());
      tag.setName(tag.getName().toLowerCase());
      tag.setValue(tag.getValue().toLowerCase());
      
      try {
        TagType type = TagType.valueOf(tag.getType());
        TagDefinition tagDef = tagDefs.get(tag.getType() + tag.getName());
        
        if(type == TagType.TIMELINE) {
          throw new ValidationException("Only Lookup and Freeform tags are accepted in tags: " + tag.getName() + " - " + tag.getType());
        }
        
        // Verify if tag values exist in parent tag definition
        if(tagDef == null) {
          throw new ValidationException("Parent tag definition does not exist");
        }
        
        // If it is a Lookup tag, check the value is defined in the parent tag
        if(type == TagType.LOOKUP && !tagDef.getValues().contains(tag.getValue())) {
          throw new ValidationException("Tag value must exist in the parent tag definition: " + tag.getValue());
        }
        
        if(type == TagType.LOOKUP) {
          lookupNames.add(tag.getName());
          lookupTags.add(tag.getName() + Globals.TAG_DELIMITER + tag.getValue());
        }
        else if(type == TagType.FREEFORM) {
          freeformNames.add(tag.getName());
          freeformTags.add(tag.getName() + Globals.TAG_DELIMITER + tag.getValue());
        }
      } catch (IllegalArgumentException e) {
        throw new ValidationException("Tag type is invalid " + tag.getType());
      }
    }
    
    if(!Utils.isEmpty(timeline)) {
      // Verify Timeline definition is valid
      try {
        tl = gson.fromJson(timeline, Timeline.class);
        autoTrashDate = !Utils.isEmpty(tl.getEndDate()) ? dateFormat.parse(tl.getEndDate()) : null;
      }
      catch (JsonSyntaxException e) {
        throw new ValidationException("Timeline definition is not valid");
      }
      catch (ParseException e) {
        throw new ValidationException("Timeline end date is not valid");
      }
    }
    
    TaggedStorageObject tso = new TaggedStorageObject(companyId, objectId, 
        new ArrayList<String>(lookupNames), new ArrayList<String>(lookupTags), 
        new ArrayList<String>(freeformNames), new ArrayList<String>(freeformTags), 
        timeline, autoTrashDate, user.getEmail());
    
    datastoreService.put(tso);
    
    return tso;
  }

  public TaggedStorageObject get(String id) throws Exception {
    return (TaggedStorageObject) datastoreService.get(new TaggedStorageObject(id));
  }
  
  public void delete(String id) throws Exception {
    datastoreService.delete(new TaggedStorageObject(id));
  }

  public PagedResult<TaggedStorageObject> list(String companyId, String search, Integer limit, String sort, String cursor) throws Exception {
    return datastoreService.list(TaggedStorageObject.class, limit, sort, cursor, mergeFilters(parseQuery(search), "companyId", companyId));
  }
  
  public List<TaggedStorageObject> listFilesByTags(String companyId, List<Tag> tags) throws Exception {
    // Finds all TagEntries with any of the given tag names (values are processed in the next stage)
    List<Object> params = new ArrayList<Object>();
    List<String> lookupNames = new ArrayList<String>();
    List<String> lookupTags = new ArrayList<String>();
    List<String> freeformNames = new ArrayList<String>();
    List<String> freeformTags = new ArrayList<String>();
    
    // Create lists of strings matching the stored format
    for(Tag tag : tags) {
      if(TagType.valueOf(tag.getType()) == TagType.LOOKUP) {
        if(Utils.isEmpty(tag.getValue())) {
          lookupNames.add(tag.getName());
        }
        else {
          lookupTags.add(tag.getName() + Globals.TAG_DELIMITER + tag.getValue());
        }
      }
      else if(TagType.valueOf(tag.getType()) == TagType.FREEFORM) {
        if(Utils.isEmpty(tag.getValue())) {
          freeformNames.add(tag.getName());
        }
        else {
          freeformTags.add(tag.getName() + Globals.TAG_DELIMITER + tag.getValue());
        }
      }
    }
    
    params.add("companyId");
    params.add(companyId);
    addCollectionCondition(params, "lookupNames", lookupNames);
    addCollectionCondition(params, "lookupTags", lookupTags);
    addCollectionCondition(params, "freeformNames", freeformNames);
    addCollectionCondition(params, "freeformTags", freeformTags);
    
    PagedResult<TaggedStorageObject> entries = 
        datastoreService.list(TaggedStorageObject.class, null, null, null, params.toArray());
    
    return entries.getList();
  }
  
  protected Map<String, TagDefinition> getTagDefinitions(String companyId) throws Exception {
    PagedResult<TagDefinition> tagDefs = new TagDefinitionAccessor().list(companyId, null, null, null, null);
    Map<String, TagDefinition> tags = new HashMap<String, TagDefinition>();
    
    for(TagDefinition tagDef : tagDefs.getList()) {
      tags.put(tagDef.getType() + tagDef.getName(), tagDef);
    }
    
    return tags;
  }
  
  protected void addCollectionCondition(List<Object> conditions, String field, List<String> values) {
    if(values.size() > 0) {
      conditions.add(field);
      conditions.add(values);
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(T... values) {
    return Arrays.asList(values);
  }
}
