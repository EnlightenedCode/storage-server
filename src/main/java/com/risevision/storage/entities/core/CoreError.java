package com.risevision.storage.entities.core;

import java.util.List;

public class CoreError {
  private Integer code;
  private String message;
  private List<CoreErrorItem> errors;
  
  public CoreError() {
    
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<CoreErrorItem> getErrors() {
    return errors;
  }

  public void setErrors(List<CoreErrorItem> errors) {
    this.errors = errors;
  }
}
