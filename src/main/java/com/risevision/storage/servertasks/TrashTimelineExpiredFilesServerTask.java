package com.risevision.storage.servertasks;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import com.google.api.services.storage.Storage;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.ImmutableList;
import com.risevision.storage.Globals;
import com.risevision.storage.api.accessors.AutoTrashTagAccessor;
import com.risevision.storage.datastore.DatastoreService.PagedResult;
import com.risevision.storage.entities.AutoTrashTag;
import com.risevision.storage.gcs.StorageService;
import com.risevision.storage.info.ServiceFailedException;

public class TrashTimelineExpiredFilesServerTask extends BatchServerTask {
  private StorageService storageService;
  private AutoTrashTagAccessor autoTrashTagAccessor;
  
  public TrashTimelineExpiredFilesServerTask(Storage client, Map<String, String[]> params) throws IOException {
    super(client, params);
    
    this.storageService = new StorageService(client);
    this.autoTrashTagAccessor = new AutoTrashTagAccessor();
  }

  @Override
  void handleRequest() throws IOException {
    PagedResult<AutoTrashTag> result = autoTrashTagAccessor.listExpired(new Date(), maxResults.intValue(), null, pageToken);
    
    for(AutoTrashTag tag : result.getList()) {
      try {
        storageService.moveToTrash(Globals.COMPANY_BUCKET_PREFIX + tag.getCompanyId(), ImmutableList.of(tag.getObjectId()));
        autoTrashTagAccessor.delete(tag.getId());
      } catch (ServiceFailedException e) {
        log.log(Level.WARNING, "Move to trash failed", e);
      } catch (Exception e) {
        log.log(Level.WARNING, "Delete auto trash tag failed", e);
      }
    }
    
    submitNextTask(result.getCursor());
  }

  void submitNextTask(String nextPageToken) throws IOException {
    if ((String) listResult.get("nextPageToken") != null) {
      TaskOptions options = TaskOptions.Builder.withUrl("/servertask")
          .method(TaskOptions.Method.valueOf("GET"))
          .param("task", "TrashTimelineExpiredFiles")
          .param("pageToken", nextPageToken);
        
      QueueFactory.getDefaultQueue().add(options);
    }
  }
}
