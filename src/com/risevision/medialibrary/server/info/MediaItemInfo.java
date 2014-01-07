//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package com.risevision.medialibrary.server.info;

import java.io.Serializable;
import java.util.Date;

import com.risevision.common.client.utils.RiseUtils;

/**
 * A structure representing a single object stored in S3.  Returned as a part of ListBucketResponse.
 */
@SuppressWarnings("serial")
public class MediaItemInfo implements Serializable, SearchSortable {
	public static final String KEY_ATTRIBUTE = "Key";
	public static final String LAST_MODIFIED_ATTRIBUTE = "LastModified";
	public static final String SIZE_ATTRIBUTE = "Size";
	
//	public static final String MEDIA_LIBRARY_URL = "http://s3.amazonaws.com/";
	public static final String MEDIA_LIBRARY_URL = "http://commondatastorage.googleapis.com/";

	
    /**
     * The name of the object
     */
    private String key;

    /**
     * The date at which the object was last modified.
     */
    private Date lastModified;

    /**
     * The object's ETag, which can be used for conditional GETs.
     */
    private String eTag;

    /**
     * The size of the object in bytes.
     */
    private long size;

    /**
     * The object's storage class
     */
    private String storageClass;

    /**
     * The object's owner
     */
    private Owner owner;

    public String toString() {
        return key;
    }
        
    public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	public String getSizeString() {
		String sizeString = "";
		
		if (size < 1000) {
			sizeString = size + " bytes";
		}
		else if (size > 1024 && size < Math.pow(1024, 2)) {
			sizeString = Math.floor(size / (1024 / 10)) / 10.0 + " KB";
		}
		else if (size >= Math.pow(1024, 2) && size < Math.pow(1024, 3)) {
			sizeString = Math.floor(size / (Math.pow(1024, 2) / 10)) / 10.0 + " MB";
		}
		else if (size >= Math.pow(1024, 3)) {
			sizeString = Math.floor(size / (Math.pow(1024, 3) / 10)) / 10.0 + " GB";
		}
		
		return sizeString;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public boolean search(String query) {
		if (!RiseUtils.strIsNullOrEmpty(this.key)) {
			return this.key.toLowerCase().contains(query.toLowerCase());
		}
		
		return false;
	}
	
	public int compare(SearchSortable item, String column) {
		if (item instanceof MediaItemInfo) {
			MediaItemInfo mediaItem = (MediaItemInfo) item;
			if (LAST_MODIFIED_ATTRIBUTE.equals(column) && this.lastModified != null) {
				return this.lastModified.compareTo(mediaItem.getLastModified());
			}
			else if (KEY_ATTRIBUTE.equals(column)) {
				return this.key.toLowerCase().compareTo(mediaItem.key.toLowerCase());
			}
			else if (SIZE_ATTRIBUTE.equals(column)) {
				return this.size == mediaItem.size ? 0 : this.size > mediaItem.size ? 1 : -1;
			}
		}
		
		return -1;
	}
	
	public static class Owner implements Serializable {
		private String id;
        private String displayName;
        
        public String getId() {
			return id;
		}
		
        public void setId(String id) {
			this.id = id;
		}
		
		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

    }
}
