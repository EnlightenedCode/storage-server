package com.risevision.storage.api.responses;

import java.util.List;

public class ListResponse<T> extends SimpleResponse {
  private List<T> items;
  private String cursor;
  
  public ListResponse(String userEmail, List<T> response) {
    this(true, 200, "success", userEmail, response, null);
  }
  
  public ListResponse(String userEmail, List<T> response, String cursor) {
    this(true, 200, "success", userEmail, response, cursor);
  }
  
  public ListResponse(Boolean result, Integer code, String message, String userEmail, List<T> items, String cursor) {
    super(result, code, message, userEmail);
    
    this.items = items;
    this.cursor = cursor;
  }
  
  public List<T> getItems() {
    return items;
  }
  
  public String getCursor() {
    return cursor;
  }
}
