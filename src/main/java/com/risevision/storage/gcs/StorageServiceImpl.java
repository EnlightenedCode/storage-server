package com.risevision.storage.gcs;

import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;

import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import com.google.api.services.storage.Storage.Buckets.*;
import com.google.api.services.storage.Storage.Objects.*;
import com.google.api.client.http.ByteArrayContent;
import com.risevision.storage.Globals;

public class StorageServiceImpl extends MediaLibraryService {
  private static HttpRequestInitializer credential;
  private static Storage storage;
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();

  static {
    if (Globals.devserver) {
      credential = LocalCredentialBuilder.getCredentialFromP12File();
    } else {
      credential = new AppIdentityCredential(Arrays.asList(Globals.STORAGE_SCOPE));
    }

    storage = new Storage.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
               .setApplicationName(Globals.STORAGE_APP_NAME)
               .build();
  }

  @Override
  public ListAllMyBucketsResponse getAllMyBuckets()
  throws ServiceFailedException {
    return null;
  }

  @Override
  public InputStream getBucketProperty(String bucketName, String property)
  throws ServiceFailedException {
    return null;
  }

  @Override
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
      } catch (IOException e) {
        log.warning(e.getMessage());
        throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
      }

      try {
        for (String folderName : listResult.getPrefixes()) {
          StorageObject folderItem = new StorageObject();
          folderItem.setName(folderName);
          folderItem.setKind("folder");
          items.add(folderItem);
        }
      } catch (NullPointerException e) {
        log.warning("No folders to list");
      }

      try {
        items.addAll(listResult.getItems());
      } catch (NullPointerException e) {
        log.warning("No objects to list");
      }
      listRequest.setPageToken(listResult.getNextPageToken());
    } while (null != listResult.getNextPageToken());

    return items;
  }

  @Override
  public void createBucket(String bucketName)
  throws ServiceFailedException {

    log.info("Creating bucket using gcs client library");
    Bucket newBucket = new Bucket().setName(bucketName)
                                   .setLogging(new Bucket.Logging()
                                     .setLogBucket(Globals.LOGS_BUCKET_NAME)
                                     .setLogObjectPrefix(bucketName));

    try {
      storage.buckets().insert(Globals.PROJECT_ID, newBucket).execute();
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }

  public void createFolder(String bucketName, String folderName)
  throws ServiceFailedException {
    log.info("Creating folder using gcs client library");

    StorageObject objectMetadata = new StorageObject()
      .setName(folderName + "/");
      /*.setAcl(ImmutableList.of(
            new ObjectAccessControl().setEntity("domain-example.com").setRole("READER"),
            new ObjectAccessControl().setEntity("user-administrator@example.com").setRole("OWNER")
            ))*/
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

  @Override
  public void updateBucketProperty(String bucketName, 
                                  String property,
                                  String propertyXMLdoc) 
  throws ServiceFailedException {
  }

  @Override
  public boolean deleteMediaItem(String bucketName, String itemName)
  throws ServiceFailedException {
    log.info("Deleting object using gcs client library");

    try {
      storage.objects().delete(bucketName, itemName).execute();
      return true;
    } catch (IOException e) {
      log.warning(e.getMessage());
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }
  }

  @Override
  public InputStream getMediaItem(String bucketName, String itemName)
  throws ServiceFailedException {
    return null;
  }

  @Override
  public String getMediaItemUrl(String bucketName, String itemName)
  throws Exception {
    return null;
  }

  @Override
  public String getSignedPolicy(String policyBase64) {
    return null;
  }
}
