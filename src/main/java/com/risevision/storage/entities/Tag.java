package com.risevision.storage.entities;

public class Tag {
  private String name;
  private String type;
  private String value;
  
  public Tag() {
    
  }
  
  public Tag(String name, String type, String value) {
    setName(name);
    setType(type);
    setValue(value);
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
