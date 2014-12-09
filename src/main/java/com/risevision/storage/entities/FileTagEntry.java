package com.risevision.storage.entities;

import java.util.List;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

@Subclass(index=true)
public class FileTagEntry extends DatastoreEntity {
  @Index
  public String companyId;
  @Index
  public String objectId;
  public String name;
  public String type;
  public List<String> values;

  public FileTagEntry() {
      super();
  }

  public FileTagEntry(String id) {
      super(id);
  }

  public FileTagEntry(String companyId, String objectId, String name, String type, List<String> values, String email) {
      super();
      this.companyId = companyId;
      this.objectId = objectId;
      this.name = name;
      this.type = type;
      this.values = values;
      this.setChangedBy(email);
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }
}
