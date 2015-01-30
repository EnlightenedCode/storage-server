package com.risevision.storage.entities;

import java.util.List;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Subclass;

/**
 * Created by rodrigopavezi on 12/9/14.
 */
@Entity
@Subclass(index=true)
public class TagDefinition extends DatastoreEntity {
  @Index
  public String companyId;
  @Index
  public String name;
  public String type;
  public List<String> values;
  
  public TagDefinition() {
    super();
  }

  public TagDefinition(String id) {
    super(id);
  }

  public TagDefinition(String companyId, String type, String name, List<String> values, String email) {
    super(companyId + type + name);
    
    this.companyId = companyId;
    this.type = type;
    this.name = name;
    this.values = values;
    this.setChangedBy(email);
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
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
