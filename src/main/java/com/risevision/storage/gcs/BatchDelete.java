package com.risevision.storage.gcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.risevision.storage.Globals;
import com.risevision.storage.api.accessors.FileTagEntryAccessor;
import com.risevision.storage.info.ServiceFailedException;

public class BatchDelete {
  protected static final Logger log = Logger.getAnonymousLogger();
  
  private Storage storage;
  private List<String> errorList;

  public BatchDelete(Storage storage) {
    this.storage = storage;
    this.errorList = new ArrayList<String>();
  }

  @SuppressWarnings("rawtypes")
  class DeleteBatchCallback extends JsonBatchCallback {
    String fileName;

    public DeleteBatchCallback(String deleteFileName) {
      fileName = deleteFileName;
    }

    public void onSuccess(Object n, HttpHeaders responseHeaders) {
    }

    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
      if (e.getCode() != ServiceFailedException.NOT_FOUND) {
        log.warning("Could not delete " + fileName);
        errorList.add(fileName);
        log.warning(e.getMessage());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> deleteFiles(String bucketName, List<String> deleteList)
  throws ServiceFailedException {
    BatchRequest batch = storage.batch();
    errorList = new ArrayList<String>();
    Storage.Objects.List listRequest;
    com.google.api.services.storage.model.Objects listResult;
    List<String> deletedFiles = new ArrayList<String>();

    try {
      for (String deleteItem : deleteList) {
        if (deleteItem.endsWith("/")) {
          listRequest = storage.objects().list(bucketName)
                                         .setPrefix(null)
                                         .setDelimiter(null);
          do {
              listResult = listRequest.execute();
              if (listResult.getItems() != null) {
                for (StorageObject bucketItem : listResult.getItems()) {
                  if (bucketItem.getName().startsWith(deleteItem)) {
                    storage.objects().delete(bucketName,
                                           bucketItem.getName())
                       .queue(batch, new DeleteBatchCallback(bucketItem.getName()));
                    
                    if(!bucketItem.getName().endsWith("/")) {
                      deletedFiles.add(deleteItem);
                    }
                  }
                }
              }
              listRequest.setPageToken(listResult.getNextPageToken());
          } while (null != listResult.getNextPageToken());
        } else {
          storage.objects().delete(bucketName, deleteItem)
                           .queue(batch, new DeleteBatchCallback(deleteItem));
          deletedFiles.add(deleteItem);
        }
      }
      
      if(batch.size() > 0) {
        batch.execute();
      }
      
      new FileTagEntryAccessor().
        deleteTagsByObjectId(bucketName.replace(Globals.COMPANY_BUCKET_PREFIX, ""), deletedFiles);
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }

    return errorList;
  }
}
