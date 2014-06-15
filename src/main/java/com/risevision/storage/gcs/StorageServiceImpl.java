package com.risevision.storage.gcs;

import java.io.InputStream;
import java.util.List;
import java.io.Exception;
import java.util.Arrays;

import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.risevision.storage.Globals;

public class StorageServiceImpl extends MediaLibraryService {
  private static final JsonFactory JSON_FACTORY =
    JacksonFactory.getDefaultInstance();
  private static final String STORAGE_SCOPE =
    "https://www.googleapis.com/auth/devstorage.full_control";

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

  @Override
  public List<MediaItemInfo> getBucketItems(String bucketName,
                                            String prefix)
  throws ServiceFailedException {
    return null;
  }

  @Override
  public void createBucket(String bucketName)
  throws ServiceFailedException {

    log.info("Creating bucket");
    AppIdentityCredential credential = new 
      AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));
    Bucket newBucket = new Bucket().setName(bucketName)
                                   .setLogging(new Bucket.Logging()  //confirm
                                     .setLogBucket(Globals.LOGS_BUCKET_NAME)
                                     .setLogObjectPrefix(bucketName));

    Storage storage = new Storage.Builder(HTTP_TRANSPORT,
                                          JSON_FACTORY,
                                          credential).build();
    try {
      storage.buckets().insert(Globals.PROJECT_ID, newBucket).execute();
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
    return false;
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
