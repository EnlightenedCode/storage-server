package com.risevision.storage.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RvClientStorageObject {
  private String id;
  private String companyId;
  private String objectId;
  private List<Tag> tags;
  private String timeline;
  private Date creationDate;
  private Date changedDate;
  private String changedBy;
  private String createdBy;
  
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

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getChangedDate() {
    return changedDate;
  }

  public void setChangedDate(Date changedDate) {
    this.changedDate = changedDate;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
}
