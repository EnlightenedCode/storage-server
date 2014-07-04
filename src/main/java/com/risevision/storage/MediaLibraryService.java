package com.risevision.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import com.google.common.base.Strings;
import com.risevision.storage.amazonImpl.ListAllMyBucketsResponse;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.gcs.StorageServiceImpl;

public abstract class MediaLibraryService {
  private static MediaLibraryService instance; 
  private static StorageServiceImpl GCSinstance; 
  protected static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  protected static final Logger log = Logger.getAnonymousLogger();

  public static MediaLibraryService getInstance() {
    try {
      if (instance == null)
        instance = new MediaLibraryServiceImpl();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return instance;
  }

  public static StorageServiceImpl getGCSInstance() {
    try {
      if (GCSinstance == null)
        GCSinstance = new StorageServiceImpl();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return GCSinstance;
  }

  public abstract ListAllMyBucketsResponse getAllMyBuckets() 
  throws ServiceFailedException;

  public abstract InputStream getBucketProperty(String bucketName,
                                                String property)
  throws ServiceFailedException;

  public abstract String getBucketPropertyString(String bucketName,
                                                 String property)
  throws ServiceFailedException;

  public List getBucketItems(String bucketName)
  throws ServiceFailedException {
    return getBucketItems(bucketName, null, null);
  }

  public List getBucketItems(String bucketName, String prefix)
  throws ServiceFailedException {
    return getBucketItems(bucketName, prefix, null);
  }

  public abstract List getBucketItems(String bucketName,
                                                     String prefix,
                                                     String delimiter)
  throws ServiceFailedException;

  public String getBucketItemsString(String bucketName)
  throws ServiceFailedException {
    return getBucketItemsString(bucketName, null, null);
  }

  public String getBucketItemsString(String bucketName,
                                     String prefix,
                                     String marker)
  throws ServiceFailedException {
    String response = "";

    try {
      JSONWriter stringer;
      stringer = new JSONStringer();
      List<MediaItemInfo> items = getBucketItems(bucketName, prefix, marker);
      stringer.object();
      stringer.key("status").value(ServiceFailedException.OK);
      stringer.key("mediaFiles");
      stringer.array();
      for (MediaItemInfo item: items) {
        stringer.object();

        stringer.key("key").value(item.getKey());
        stringer.key("lastModified").value(item.getLastModified() != null ?
          item.getLastModified().getTime() : "");
        stringer.key("size").value(item.getSize());
        stringer.key("eTag").value(item.geteTag());

        stringer.endObject();
      }
      stringer.endArray();
      stringer.endObject();

      response = stringer.toString();		
    } catch (JSONException e) {
      log.severe("Error - " + e.getMessage());
      e.printStackTrace();
    }

    return response;
  }

  public static String getBucketName(String companyId)
  throws ServiceFailedException {
    if (Strings.isNullOrEmpty(companyId)) {
      throw new ServiceFailedException(ServiceFailedException.SERVER_ERROR);
    }

    return "risemedialibrary-" + companyId;
  }

  public abstract void createBucket(String bucketName)
  throws ServiceFailedException;

  public abstract void updateBucketProperty(String bucketName,
                                            String property,
                                            String propertyXMLdoc)
  throws ServiceFailedException;

  public abstract InputStream getMediaItem(String bucketName, String itemName)
  throws ServiceFailedException;

  public abstract String getSignedPolicy(String policyBase64);
}
