package com.risevision.storage.entities;

import com.google.api.services.storage.model.StorageObject;

import java.util.ArrayList;
import java.util.List;

public class BucketItems {
  public List<StorageObject> rootItems;
  public List<StorageObject> allFolders;

  public BucketItems (){
    rootItems = new ArrayList<StorageObject>();
    allFolders = new ArrayList<StorageObject>();
  }
}
