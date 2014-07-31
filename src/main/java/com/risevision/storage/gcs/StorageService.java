package com.risevision.storage.gcs;

import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.google.common.collect.ImmutableList;

import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpHeaders;
import java.math.BigInteger;
import com.google.api.client.util.DateTime;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.services.storage.model.BucketAccessControl;
import com.google.api.services.storage.Storage.Buckets.*;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.risevision.storage.Globals;

public final class StorageService {
  private static HttpRequestInitializer credential;
  private static Storage storage;
  private static StorageService instance;
  protected static final Logger log = Logger.getAnonymousLogger();

  static {
    if (Globals.devserver) {
      credential = LocalCredentialBuilder.getCredentialFromP12File();
    } else {
      credential = new AppIdentityCredential(Arrays.asList(Globals.STORAGE_SCOPE));
    }

    storage = GCSClient.getStorageClient(credential);
  }

  private StorageService() {}

  public static StorageService getInstance() {
    try {
      if (instance == null) {
        instance = new StorageService();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return instance;
  }

  public void setClient(Storage client) {
    this.storage = client;
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

    prefix = (prefix != null && !prefix.endsWith(delimiter)) ? 
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
    List<String> prefixes = new ArrayList<String>();

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
          new BucketAccessControl().setEntity(Globals.EDITOR_GROUP)
                                   .setRole("OWNER")));

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
      .setName(folderName + "/");
    try {
      storage.objects()
             .insert(bucketName,
                     objectMetadata,
                     ByteArrayContent.fromString("text/plain", ""))
             .execute();
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

  public InputStream getMediaItem(String bucketName, String itemName)
  throws ServiceFailedException {
    return null;
  }

  public String getSignedPolicy(String policyBase64) {
    return null;
  }

  class BatchDelete {
    List<String> errorList;

    public BatchDelete() {
      errorList = new ArrayList<String>();
    }

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

    public List<String> deleteFiles(String bucketName, List<String> deleteList)
    throws ServiceFailedException {
      BatchRequest batch = storage.batch();
      errorList = new ArrayList<String>();

      try {
        for (String item : deleteList) {
          if (item.endsWith("/")) {
            List<StorageObject> folderContents = 
              getBucketItems(bucketName, item, "/");
            for (StorageObject subItem : folderContents) {
              storage.objects().delete(bucketName,
                                       subItem.getName())
                               .queue(batch, new DeleteBatchCallback(item));
            }
          } else {
            storage.objects().delete(bucketName, item)
                             .queue(batch, new DeleteBatchCallback(item));
          }
        }

        batch.execute();
      } catch (IOException e) {
        log.warning(e.getMessage());
        throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
      }

      return errorList;
    }
  }
}
