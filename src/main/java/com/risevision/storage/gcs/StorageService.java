package com.risevision.storage.gcs;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.*;
import com.google.api.client.util.DateTime;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.*;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.risevision.storage.Globals;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.ObjectAclFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.QueueFactory;

public final class StorageService {
  static Storage storage;
  protected static final Logger log = Logger.getAnonymousLogger();

  public StorageService(Storage storage) {
    this.storage = storage;
  }

  public ListAllMyBucketsResponse getAllMyBuckets()
  throws ServiceFailedException {
    return null;
  }

  public InputStream getBucketProperty(String bucketName, String property)
  throws ServiceFailedException {
    return null;
  }

  public String getBucketPropertyString(String bucketName, String property)
  throws ServiceFailedException {
    return null;
  }

  public List<StorageObject> getBucketItems(String bucketName,
                                            String prefix,
                                            String delimiter)
  throws ServiceFailedException {
    Storage.Objects.List listRequest;

    prefix = (!Strings.isNullOrEmpty(prefix) && !prefix.endsWith(delimiter)) ? 
      prefix + delimiter : prefix;

    log.info("Fetching object list from " +
             "\nbucket: " + bucketName + 
             "\nprefix: " + prefix +
             "\ndelimiter: " + delimiter);

    try {
      listRequest = storage.objects().list(bucketName)
                                     .setPrefix(prefix)
                                     .setDelimiter(delimiter);
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }

    com.google.api.services.storage.model.Objects listResult;
    List<StorageObject> items = new ArrayList<StorageObject>();

    do {
      try {
        listResult = listRequest.execute();
      } catch (GoogleJsonResponseException e) {
        if (e.getStatusCode() != ServiceFailedException.NOT_FOUND) {
          log.warning(e.getStatusCode() + " - " + e.getMessage());
        }
        throw new ServiceFailedException(e.getStatusCode());
      }  catch (IOException e) {
        log.warning(e.getMessage());
        throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
      }

      try {
        for (String folderName : listResult.getPrefixes()) {
          if (folderName.equals(prefix)) {
            items.add(new StorageObject().setName(folderName).setKind("folder"));
            continue;
          }

          if(!folderName.equals(Globals.TRASH)) {
            StorageObject folderItem = new StorageObject();
            folderItem.setName(folderName);
            folderItem.setKind("folder");
            long folderSize = 0;
            long latestDt = 0;
            long folderFileDt = 0;
            List<StorageObject> folderFiles = getBucketItems(bucketName
                                                            ,folderName
                                                            ,"/");
            for (StorageObject folderFile : folderFiles) {
              folderSize += folderFile.getSize().longValue();
              folderFileDt = folderFile.getUpdated().getValue();
              latestDt = folderFileDt > latestDt ? folderFileDt : latestDt;
            }
            folderItem.setSize(BigInteger.valueOf(folderSize));
            folderItem.setUpdated(new DateTime(latestDt));
            items.add(folderItem);
          }
        }
      } catch (NullPointerException e) {
        log.info("No folders to list");
      }

      try {
        items.addAll(listResult.getItems());
      } catch (NullPointerException e) {
        log.info("No files to list");
      }
      listRequest.setPageToken(listResult.getNextPageToken());
    } while (null != listResult.getNextPageToken());

    return items;
  }

  public void enablePublicRead(String companyId)
  throws ServiceFailedException {
    log.info("Enabling public read for " + companyId);

    QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/servertask")
    .param("task", "AddPublicReadBucket")
    .param("bucket", Globals.COMPANY_BUCKET_PREFIX + companyId)
    .method(TaskOptions.Method.valueOf("GET")));
  }

  public void createBucket(String bucketName)
  throws ServiceFailedException {

    log.info("Creating bucket using gcs client library");
    Bucket newBucket = new Bucket()
                      .setName(bucketName)
                      .setLogging(new Bucket.Logging()
                        .setLogBucket(Globals.LOGS_BUCKET_NAME)
                        .setLogObjectPrefix(bucketName))
                      .setAcl(ImmutableList.of(
      new BucketAccessControl().setEntity("allUsers")
                               .setRole("READER"),
      new BucketAccessControl().setEntity("project-editors-" + Globals.PROJECT_ID)
                               .setRole("OWNER")))
                      .setDefaultObjectAcl(ObjectAclFactory.getDefaultAcl())
                      .setCors(ImmutableList.of(
      new Bucket.Cors().setMaxAgeSeconds(3600)
                         .setMethod(ImmutableList.of("GET", "PUT", "POST"))
                         .setOrigin(ImmutableList.of("*"))
                         .setResponseHeader(ImmutableList.of(
                         "Content-Type", "x-goog-resumable", "Content-Length"))));

    try {
      storage.buckets().insert(Globals.PROJECT_ID, newBucket).execute();
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
    log.info("Bucket created: " + bucketName);
  }

  public void deleteBucket(String bucketName)
  throws ServiceFailedException {

    log.info("Deleting bucket using gcs client library");

    try {
      storage.buckets().delete(bucketName).execute();
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
    log.info("Bucket deleted: " + bucketName);
  }

  public void createFolder(String bucketName, String folderName)
  throws ServiceFailedException {
    log.info("Creating folder using gcs client library");

    StorageObject objectMetadata = new StorageObject()
      .setName(folderName.endsWith("/") ? folderName : folderName + "/");
    try {
      storage.objects()
             .insert(bucketName,
                     objectMetadata,
                     ByteArrayContent.fromString("text/plain", ""))
             .execute();
    } catch (GoogleJsonResponseException e) {
      log.warning(e.getDetails().getMessage());
      throw new ServiceFailedException(e.getDetails().getCode());
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }

  public void updateBucketProperty(String bucketName, 
                                  String property,
                                  String propertyXMLdoc) 
  throws ServiceFailedException {
  }

  public List<String> deleteMediaItems(String bucketName, List<String> items)
  throws ServiceFailedException {
    List<String> errorItems = new ArrayList<String>();
    if (items.size() == 0) {return errorItems;}
    String plurality = items.size() > 1 ? "s" : "";
    log.info("Deleting " + Integer.toString(items.size()) +
             " object" + plurality + " from " + bucketName +
             " using gcs client library");

    if (items.size() == 1 && items.get(0).endsWith("/") == false) {
      try {
        storage.objects().delete(bucketName, items.get(0)).execute();
      } catch (GoogleJsonResponseException e) {
        if (e.getDetails().getCode() != ServiceFailedException.NOT_FOUND) {
          log.warning(e.getDetails().getMessage());
          errorItems.add(items.get(0));
        }
      } catch (IOException e) {
        log.warning(e.getMessage());
        errorItems.add(items.get(0));
      }
      return errorItems;
    }

    BatchDelete batchDelete = new BatchDelete();
    errorItems = batchDelete.deleteFiles(bucketName, items);
    return errorItems;
  }
  
  /**
   * Moves the list of provided items into the trash folder 
   *  
   * @param bucketName The bucket containing the items
   * @param items The items to move
   * @return The list of files that were not moved successfully
   * 
   * @throws ServiceFailedException In case an unexpected error occurs
   */
  public List<String> moveToTrash(String bucketName, List<String> items) throws ServiceFailedException {
    if(!objectExists(bucketName, Globals.TRASH)) {
      // Create the Trash folder
      createFolder(bucketName, Globals.TRASH);
    }
    
    return new BatchMove(storage).execute(bucketName, items, Globals.TRASH);
  }
  
  /**
   * Moves the list of provided items from trash into their original folders 
   *  
   * @param bucketName The bucket containing the items
   * @param items The items to move
   * @return The list of files that were not restored successfully
   * 
   * @throws ServiceFailedException In case an unexpected error occurs
   */
  public List<String> restoreFromTrash(String bucketName, List<String> items) throws ServiceFailedException {
    return new BatchRestore(storage).execute(bucketName, items);
  }
  
  /**
   * Moves the list of provided items into destinationFolder (copies + deletes). 
   *  
   * @param bucketName The bucket containing the items
   * @param items The items to move
   * @param destinationFolder The destination where to move the files (new prefix name)
   * @return The list of files that were not moved successfully
   * 
   * @throws ServiceFailedException In case an unexpected error occurs
   */
  public List<String> moveMediaItems(String bucketName, List<String> items, String destinationFolder) throws ServiceFailedException {
    return new BatchMove(storage).execute(bucketName, items, destinationFolder);
  }

  public InputStream getMediaItem(String bucketName, String itemName)
  throws ServiceFailedException {
    return null;
  }

  public String getResumableUploadURI(String bucketId,
                                      String fileName,
                                      String fileType,
                                      String origin)
  throws ServiceFailedException {
    HttpResponse response; HttpRequest request;

    String requestURI = new String(Globals.RESUMABLE_UPLOAD_REQUEST_URI)
                            .replace("myBucket", bucketId)
                            .concat(fileName);
    try {
      request = storage.getRequestFactory()
                       .buildPostRequest(new GenericUrl(requestURI), null);
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
    
    request.setHeaders(new HttpHeaders()
                      .setContentLength(0L)
                      .set("origin", origin)
                      .set("X-Upload-Content-Type", fileType));
    log.info("Requesting uri for " + fileName);
    log.info(request.getHeaders().toString());
    try {
      response = request.execute();
    } catch (HttpResponseException e) {
      log.warning(e.getContent());
      throw new ServiceFailedException(e.getStatusCode());
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
    log.info("Upload uri received");
    return response.getHeaders().getLocation();
  }

  public String getSignedDownloadURI(String bucketId, String fileName, String fileType) throws ServiceFailedException {
    String signedURI;
    
    try {
      if(Globals.devserver) {
        signedURI = LocalSignedURIGenerator.getSignedURI("GET", bucketId, fileName);
      }
      else {
        signedURI = SignedURIGenerator.getSignedURI("GET", bucketId, fileName);
      }
    } catch (Exception e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
    
    log.info("Signed URL for " + fileName + ": " + signedURI);
    
    return signedURI;
  }
  
  class BatchDelete {
    List<String> errorList;

    public BatchDelete() {
      errorList = new ArrayList<String>();
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
                    }
                  }
                }
                listRequest.setPageToken(listResult.getNextPageToken());
            } while (null != listResult.getNextPageToken());
          } else {
            storage.objects().delete(bucketName, deleteItem)
                             .queue(batch, new DeleteBatchCallback(deleteItem));
          }
        }
        
        if(batch.size() > 0) {
          batch.execute();
        }
      } catch (IOException e) {
        log.warning(e.getMessage());
        throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
      }

      return errorList;
    }
  }
  
  public boolean objectExists(String bucketName, String objectName) {
    try {
      Storage.Objects.Get getRequest = storage.objects().get(bucketName, objectName);
      
      // If objectName does not exist, an exception will be thrown and the method will return false
      getRequest.execute();
      
      return true;
    } catch (IOException e) {
      return false;
    }    
  }
}
