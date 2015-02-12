package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.google.api.services.storage.Storage;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.risevision.storage.Utils;
import com.risevision.storage.api.accessors.FileTagEntryAccessor;
import com.risevision.storage.api.accessors.RvStorageObjectAccessor;
import com.risevision.storage.api.wrapper.RvStorageOutputWrapper;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.FileTagEntry;
import com.risevision.storage.entities.Tag;
import com.risevision.storage.info.ServiceFailedException;

public class ConvertTaggingDataFormatServerTask extends BatchServerTask {
  private static Map<String, Map<String, RvStorageOutputWrapper>> companies;
  
  private FileTagEntryAccessor fileTagEntryAccessor;
  private RvStorageObjectAccessor rvStorageObjectAccessor;
  
  public ConvertTaggingDataFormatServerTask(Storage client, Map<String, String[]> params) throws IOException {
    super(client, params);
    
    if(Utils.isEmpty(pageToken)) {
      companies = new HashMap<String, Map<String, RvStorageOutputWrapper>>();
    }
    
    this.fileTagEntryAccessor = new FileTagEntryAccessor();
    this.rvStorageObjectAccessor = new RvStorageObjectAccessor();
  }

  @Override
  void handleRequest() throws IOException {
    try {
      PagedResult<FileTagEntry> result = fileTagEntryAccessor.list("", maxResults.intValue(), null, pageToken);      
      
      for(FileTagEntry tag : result.getList()) {
        Map<String, RvStorageOutputWrapper> companyFiles = companies.get(tag.getCompanyId());
        
        if(companyFiles == null) {
          companyFiles = new HashMap<String, RvStorageOutputWrapper>();
          companies.put(tag.getCompanyId(), companyFiles);
        }
        
        RvStorageOutputWrapper file = companyFiles.get(tag.getObjectId());
        
        if(file == null) {
          file = new RvStorageOutputWrapper();
          companyFiles.put(tag.getObjectId(), file);
        }
        
        file.setCreatedBy(tag.getCreatedBy());
        file.setChangedBy(tag.getChangedBy());
        file.setCreationDate(tag.getCreationDate());
        file.setChangedDate(tag.getChangedDate());
        
        for(String value : tag.getValues()) {
          if(tag.getType().equals("LOOKUP") || tag.getType().equals("FREEFORM")) {
            file.getTags().add(new Tag(tag.getType(), tag.getName(), value));
          }
          else if(tag.getType().equals("TIMELINE")) {
            file.setTimeline(value);
          }
        }
      }
      
      if (result.getCursor() == null) {
        for(String companyId : companies.keySet())  {
          Map<String, RvStorageOutputWrapper> companyFiles = companies.get(companyId);
          
          for(String objectId : companyFiles.keySet()) {
            RvStorageOutputWrapper file = companyFiles.get(objectId);
            User user = new User(file.getCreatedBy(), "");
            
            rvStorageObjectAccessor.put(companyId, objectId, file.getTags(), file.getTimeline(), user);
          }
        }
      }
      
      submitNextTask(result.getCursor());
    } catch (ServiceFailedException e) {
      log.log(Level.WARNING, "Move to trash failed", e);
    } catch (Exception e) {
      log.log(Level.WARNING, "Delete auto trash tag failed", e);
    }
  }

  void submitNextTask(String nextPageToken) throws IOException {
    if (nextPageToken != null) {
      TaskOptions options = TaskOptions.Builder.withUrl("/servertask")
          .method(TaskOptions.Method.valueOf("GET"))
          .param("task", "ConvertTaggingDataFormat")
          .param("pageToken", nextPageToken);
        
      QueueFactory.getDefaultQueue().add(options);
    }
  }
}
