package com.risevision.storage.entities;

import java.util.Date;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

@Subclass(index = true)
public class AutoTrashTag extends DatastoreEntity {
  @Index
  private String companyId;
  @Index
  private String objectId; // An object can only have one Timeline tag
  @Index
  private Date expiration;
  
  public AutoTrashTag(String id) {
    super(id);
  }
  
  public AutoTrashTag(String companyId, String objectId, Date expiration, String email) {
    setId(companyId + objectId);
    
    setCompanyId(companyId);
    setObjectId(objectId);
    setExpiration(expiration);
    
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

  public Date getExpiration() {
    return expiration;
  }

  public void setExpiration(Date expiration) {
    this.expiration = expiration;
  }
}
