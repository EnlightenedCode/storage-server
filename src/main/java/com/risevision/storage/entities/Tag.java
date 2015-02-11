package com.risevision.storage.entities;

import com.risevision.storage.Globals;

public class Tag {
  private String type;
  private String name;
  private String value;
  
  public Tag() {
    
  }
  
  public Tag(String type, String nameValue) {
    int idx = nameValue.indexOf(Globals.TAG_DELIMITER);
    
    setType(type);
    setName(nameValue.substring(0, idx));
    setValue(nameValue.substring(idx + 1));
  }
  
  public Tag(String type, String name, String value) {
    setType(type);
    setName(name);
    setValue(value);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
