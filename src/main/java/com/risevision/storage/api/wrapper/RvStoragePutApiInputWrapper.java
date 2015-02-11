package com.risevision.storage.api.wrapper;

import java.util.List;

import com.risevision.storage.entities.Tag;

public class RvStoragePutApiInputWrapper {
  private String companyId;
  private String objectId;
  private List<Tag> tags;
  private String timeline;
  private String updateOnly;
  
  public RvStoragePutApiInputWrapper() {
    
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

  public String getUpdateOnly() {
    return updateOnly;
  }

  public void setUpdateOnly(String updateOnly) {
    this.updateOnly = updateOnly;
  }
}
