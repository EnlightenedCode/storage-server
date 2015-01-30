package com.risevision.storage.gcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.risevision.storage.ObjectAclFactory;
import com.risevision.storage.info.ServiceFailedException;

public class BatchRestore {
  protected static final Logger log = Logger.getAnonymousLogger();

  private Storage storage;
  private List<String> errorList;

  public BatchRestore(Storage storage) {
    this.storage = storage;
    this.errorList = new ArrayList<String>();
  }

  @SuppressWarnings("rawtypes")
  class BatchRestoreCallback extends JsonBatchCallback {
    String fileName;

    public BatchRestoreCallback(String fileName) {
      this.fileName = fileName;
    }

    public void onSuccess(Object n, HttpHeaders responseHeaders) {
      
    }

    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
      if (e.getCode() != ServiceFailedException.NOT_FOUND) {
        log.warning("Could not restore " + fileName);
        errorList.add(fileName);
        log.warning(e.getMessage());
      }
    }
  }

  public List<String> execute(String bucketName, List<String> itemList)
      throws ServiceFailedException {
    BatchRequest batchCopy = storage.batch();
    BatchRequest batchDelete = storage.batch();
    errorList = new ArrayList<String>();
    Storage.Objects.List listRequest;
    com.google.api.services.storage.model.Objects listResult;

    try {
      for (String item : itemList) {
        if (item.endsWith("/")) {
          listRequest = storage.objects().list(bucketName).setPrefix(item).setDelimiter(null);
          
          do {
            listResult = listRequest.execute();
            
            for (StorageObject bucketItem : listResult.getItems()) {
              String objName = bucketItem.getName();
              
              if (objName.startsWith(item)) {
                if (objName.endsWith("/")) {
                  restoreFolder(bucketName, bucketItem, batchDelete);
                }
                else {
                  restoreFile(bucketName, bucketItem, batchCopy, batchDelete);
                }
              }
            }
            listRequest.setPageToken(listResult.getNextPageToken());
          } while (null != listResult.getNextPageToken());
        } else {
          restoreFile(bucketName, storage.objects().get(bucketName, item).execute(), batchCopy, batchDelete);
        }
      }

      if(batchCopy.size() > 0) {
        batchCopy.execute();
      }
      
      if(batchDelete.size() > 0) {
        batchDelete.execute();
      }
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }

    return errorList;
  }
  
  @SuppressWarnings("unchecked")
  public void restoreFolder(String bucketName, StorageObject item, BatchRequest batchDelete) throws IOException {
    String newName = item.getMetadata().get(BatchMove.FORMER_FILE_NAME);
    StorageObject storageObject = new StorageObject().setName(newName).setMetadata(item.getMetadata());
    
    rebuildParentPath(bucketName, newName);
    
    storage.objects().insert(bucketName, storageObject, ByteArrayContent.fromString("text/plain", ""))
      .execute();
    
    storage.objects().delete(bucketName, item.getName())
        .queue(batchDelete, new BatchRestoreCallback(item.getName()));    
  }
  
  @SuppressWarnings("unchecked")
  public void restoreFile(String bucketName, StorageObject item, BatchRequest batchCopy, BatchRequest batchDelete) throws IOException {
    String newName = item.getMetadata().get(BatchMove.FORMER_FILE_NAME);
    StorageObject storageObject = new StorageObject().setMetadata(item.getMetadata())
        .setContentType(item.getContentType());
    
    if(objectExists(bucketName, newName)) {
      int lastDot = newName.lastIndexOf(".");
      String baseName = lastDot >= 0 ? newName.substring(0, lastDot) : newName;
      String extension = lastDot >= 0 ? "." + newName.substring(lastDot + 1) : "";
      
      // Uses the first free name of the form: filename({idx}).ext
      for(int i = 1; ; i++) {
        newName = baseName + "(" + i + ")" + extension;
        
        if(!objectExists(bucketName, newName)) {
          break;
        }
      }
    }
    else {
      rebuildParentPath(bucketName, newName);
    }
    
    storageObject.setAcl(ObjectAclFactory.getDefaultAcl());
    
    storage.objects().copy(bucketName, item.getName(), bucketName, newName, storageObject)
      .queue(batchCopy, new BatchRestoreCallback(item.getName()));
    storage.objects().delete(bucketName, item.getName())
        .queue(batchDelete, new BatchRestoreCallback(item.getName()));
  }
  
  public void rebuildParentPath(String bucketName, String item) throws IOException {
    String path = "";
    
    for(String part : StringUtils.split(item.replace(getShortName(item), ""), "/")) {
      path += part + "/";
      
      if(!objectExists(bucketName, path)) {
        StorageObject storageObject = new StorageObject().setName(path);
        
        storage.objects().insert(bucketName, storageObject, ByteArrayContent.fromString("text/plain", ""))
          .execute();
      }
    }
  }
  
  public String getShortName(String name) {
    int nameStart;
    
    if(name.endsWith("/")) {
      nameStart = name.substring(0, name.length() - 1).lastIndexOf("/");
    }
    else {
      nameStart = name.lastIndexOf("/");
    }
    
    if(nameStart >= 0) {
      name = name.substring(nameStart + 1);
    }
    
    return name;
  }
  
  public boolean objectExists(String bucketName, String objectName) {
    try {
      Storage.Objects.Get getRequest = storage.objects().get(bucketName, objectName);
      
      // If objectName does not exist, an exception will be thrown
      getRequest.execute();
      
      return true;
    } catch (IOException e) {
      return false;
    }    
  }
}
