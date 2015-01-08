package com.risevision.storage.entities;

import java.util.ArrayList;
import java.util.List;

public class StorageEntity {
  private String name;
  private List<FileTagEntry> tags;
  
  public StorageEntity(String name) {
    this(name, new ArrayList<FileTagEntry>());
  }
  
  public StorageEntity(String name, List<FileTagEntry> tags) {
    setName(name);
    setTags(tags);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<FileTagEntry> getTags() {
    return tags;
  }

  public void setTags(List<FileTagEntry> tags) {
    this.tags = tags;
  }
}
