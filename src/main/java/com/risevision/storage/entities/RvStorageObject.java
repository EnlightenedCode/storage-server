package com.risevision.storage.entities;

import java.util.Date;
import java.util.List;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

@Entity
@Subclass(index = true)
public class RvStorageObject extends DatastoreEntity {
  @Index
  private String companyId;
  @Index
  private String objectId;
  @Index
  private List<String> lookupNames;
  @Index
  private List<String> lookupTags;
  @Index
  private List<String> freeformNames;
  @Index
  private List<String> freeformTags;
  private String timeline;
  @Index
  private Date autoTrashDate;
  
  public RvStorageObject() {
    
  }
  
  public RvStorageObject(String id) {
    setId(id);
  }
  
  public RvStorageObject(String companyId, String objectId, 
      List<String> lookupNames, List<String> lookupTags, List<String> freeformNames, List<String> freeformTags, 
      String timeline, Date autoTrashDate, String email) {
    setId(companyId + objectId);
    setCompanyId(companyId);
    setObjectId(objectId);
    setLookupNames(lookupNames);
    setLookupTags(lookupTags);
    setFreeformNames(freeformNames);
    setFreeformTags(freeformTags);
    setTimeline(timeline);
    setAutoTrashDate(autoTrashDate);
    setChangedBy(email);
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

  public List<String> getLookupNames() {
    return lookupNames;
  }

  public void setLookupNames(List<String> lookupNames) {
    this.lookupNames = lookupNames;
  }

  public List<String> getLookupTags() {
    return lookupTags;
  }

  public void setLookupTags(List<String> lookupTags) {
    this.lookupTags = lookupTags;
  }

  public List<String> getFreeformNames() {
    return freeformNames;
  }

  public void setFreeformNames(List<String> freeformNames) {
    this.freeformNames = freeformNames;
  }

  public List<String> getFreeformTags() {
    return freeformTags;
  }

  public void setFreeformTags(List<String> freeformTags) {
    this.freeformTags = freeformTags;
  }

  public String getTimeline() {
    return timeline;
  }

  public void setTimeline(String timeline) {
    this.timeline = timeline;
  }

  public Date getAutoTrashDate() {
    return autoTrashDate;
  }

  public void setAutoTrashDate(Date autoTrashDate) {
    this.autoTrashDate = autoTrashDate;
  }
}
