package com.risevision.storage.api.wrapper;

import java.util.List;

import com.risevision.storage.entities.Tag;

public class ListByTagsApiInputWrapper {
  private String companyId;
  private List<Tag> tags;
  
  public ListByTagsApiInputWrapper() {
    
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }
}
