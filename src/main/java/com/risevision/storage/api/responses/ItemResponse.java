package com.risevision.storage.api.responses;

public class ItemResponse<T> extends SimpleResponse {
  private T item;
  
  public ItemResponse(String userEmail, T response) {
    this(true, 200, "success", userEmail, response);
  }
  
  public ItemResponse(Boolean result, Integer code, String message, String userEmail, T item) {
    super(result, code, message, userEmail);
    
    this.item = item;
  }
  
  public T getItem() {
    return item;
  }
}
