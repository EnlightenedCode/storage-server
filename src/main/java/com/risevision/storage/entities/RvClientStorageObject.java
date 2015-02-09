package com.risevision.storage.entities;

import java.util.ArrayList;
import java.util.List;

public class RvClientStorageObject {
  private String id;
  private String companyId;
  private String objectId;
  private List<Tag> tags;
  private String timeline;
  
  public RvClientStorageObject() {
    setTags(new ArrayList<Tag>());
  }
  
  public RvClientStorageObject(String id, String companyId, String objectId, List<Tag> tags, String timeline) {
    this.id = id;
    this.companyId = companyId;
    this.objectId = objectId;
    this.tags = tags;
    this.timeline = timeline;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  public String getTimeline() {
    return timeline;
  }

  public void setTimeline(String timeline) {
    this.timeline = timeline;
  }
}
