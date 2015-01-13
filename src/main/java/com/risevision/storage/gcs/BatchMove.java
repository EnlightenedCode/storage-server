package com.risevision.storage.gcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.risevision.storage.Globals;
import com.risevision.storage.Utils;
import com.risevision.storage.api.accessors.FileTagEntryAccessor;
import com.risevision.storage.info.ServiceFailedException;

public class BatchMove {
  public static final String FORMER_FILE_NAME = "formerFileName";

  protected static final Logger log = Logger.getAnonymousLogger();

  private Storage storage;
  private List<String> errorList;
  
  enum Operation {
    COPY, DELETE
  }

  public BatchMove(Storage storage) {
    this.storage = storage;
    this.errorList = new ArrayList<String>();
  }

  @SuppressWarnings("rawtypes")
  class BatchRenameCallback extends JsonBatchCallback {
    Operation operation;
    String fileName;

    public BatchRenameCallback(Operation operation, String fileName) {
      this.operation = operation;
      this.fileName = fileName;
    }

    public void onSuccess(Object n, HttpHeaders responseHeaders) {
      
    }

    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
      if (e.getCode() != ServiceFailedException.NOT_FOUND) {
        log.warning("Could not " + operation.toString().toLowerCase() + " " + fileName);
        errorList.add(fileName);
        log.warning(e.getMessage());
      }
    }
  }

  public List<String> execute(String bucketName, List<String> itemList, String destinationFolder)
      throws ServiceFailedException {
    BatchRequest batchCopy = storage.batch();
    BatchRequest batchDelete = storage.batch();
    errorList = new ArrayList<String>();
    Storage.Objects.List listRequest;
    com.google.api.services.storage.model.Objects listResult;
    List<String> updatedFiles = new ArrayList<String>();

    try {
      for (String item : itemList) {
        if (item.endsWith("/")) {
          String prefix = item.replace(getShortName(item), "");
          
          listRequest = storage.objects().list(bucketName).setPrefix(item).setDelimiter(null);
          
          do {
            listResult = listRequest.execute();
            for (StorageObject bucketItem : listResult.getItems()) {
              String objName = bucketItem.getName();
              
              if (objName.startsWith(item)) {
                if (objName.endsWith("/")) {
                  moveFolder(bucketName, !objName.equals(item) ? prefix : "", bucketItem, objName, destinationFolder, batchDelete);
                }
                else {
                  moveFile(bucketName, prefix, bucketItem, objName, destinationFolder, batchCopy, batchDelete);
                  updatedFiles.add(objName);
                }
              }
            }
            listRequest.setPageToken(listResult.getNextPageToken());
          } while (null != listResult.getNextPageToken());
        } else {
          moveFile(bucketName, "", storage.objects().get(bucketName, item).execute(), item, destinationFolder, batchCopy, batchDelete);
        }
      }
      
      if(batchCopy.size() > 0) {
        batchCopy.execute();
      }
      
      if(batchDelete.size() > 0) {
        batchDelete.execute();
      }
      
      new FileTagEntryAccessor().
        updateObjectId(bucketName.replace(Globals.COMPANY_BUCKET_PREFIX, ""), updatedFiles, Utils.addPrefix(updatedFiles, Globals.TRASH));
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }

    return errorList;
  }
  
  @SuppressWarnings("unchecked")
  public void moveFolder(String bucketName, String parentPath, StorageObject bucketItem, String folderName, String destinationFolder, BatchRequest batchDelete) throws IOException {
    String newName = destinationFolder + folderName.replace(parentPath, "");
    StorageObject storageObject = newWithMetadata(bucketItem, FORMER_FILE_NAME, folderName).setName(newName);
    
    storage.objects().insert(bucketName, storageObject, ByteArrayContent.fromString("text/plain", ""))
      .execute();
    
    storage.objects().delete(bucketName, folderName)
        .queue(batchDelete, new BatchRenameCallback(Operation.DELETE, folderName));    
  }
  
  @SuppressWarnings("unchecked")
  public void moveFile(String bucketName, String parentPath, StorageObject bucketItem, String filename, String destinationFolder, BatchRequest batchCopy, BatchRequest batchDelete) throws IOException {
    String newName = destinationFolder + filename.replace(parentPath, "");
    StorageObject storageObject = newWithMetadata(bucketItem, FORMER_FILE_NAME, filename)
        .setContentType(bucketItem.getContentType());
    
    storage.objects().copy(bucketName, filename, bucketName, newName, storageObject)
        .queue(batchCopy, new BatchRenameCallback(Operation.COPY, filename));
    
    storage.objects().delete(bucketName, filename)
        .queue(batchDelete, new BatchRenameCallback(Operation.DELETE, filename));    
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
  
  protected StorageObject newWithMetadata(StorageObject base, String... metadata) {
    StorageObject obj = new StorageObject();
    Map<String, String> map = new HashMap<String, String>();
    
    if(base.getMetadata() != null) {
      map.putAll(base.getMetadata());
    }
    
    for(int i = 0; i < metadata.length; i += 2) {
      map.put(metadata[i], metadata[i + 1]);
    }
    
    obj.setMetadata(map);
    
    return obj;
  }
}
