package com.risevision.storage.entities.core;

public class CoreResponse<T> {
  private T item;
  private CoreError error;
  private String kind;
  private String etag;
  
  public CoreResponse() {
    
  }

  public T getItem() {
    return item;
  }

  public void setItem(T item) {
    this.item = item;
  }

  public CoreError getError() {
    return error;
  }

  public void setError(CoreError error) {
    this.error = error;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public String getEtag() {
    return etag;
  }

  public void setEtag(String etag) {
    this.etag = etag;
  }
}
