package com.risevision.storage.entities;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionStatus {
  enum Status {
    ON_TRIAL("On Trial"),
    TRIAL_EXPIRED("Trial Expired"),
    SUBSCRIBED("Subscribed"),
    SUSPENDED("Suspended"),
    CANCELLED("Cancelled"),
    FREE("Free"),
    NOT_SUBSCRIBED("Not Subscribed"),
    PRODUCT_NOT_FOUND("Product Not Found"),
    COMPANY_NOT_FOUND("Company Not Found"),
    ERROR("Error");
    
    String description;
    
    Status(String description) {
      this.description = description;
    }
  };
  
  private static List<String> ACTIVE_VALUES;
  
  private String pc;
  private String status;
  private String expiry;
  private String trialPeriod;
  
  static {
    ACTIVE_VALUES = buildList(Status.FREE, Status.ON_TRIAL, Status.SUBSCRIBED);
  }

  public String getPc() {
    return pc;
  }

  public void setPc(String pc) {
    this.pc = pc;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExpiry() {
    return expiry;
  }

  public void setExpiry(String expiry) {
    this.expiry = expiry;
  }

  public String getTrialPeriod() {
    return trialPeriod;
  }

  public void setTrialPeriod(String trialPeriod) {
    this.trialPeriod = trialPeriod;
  }
  
  public boolean isActive() {
    if(getStatus() == null) {
      return false;
    }
    else {
      return ACTIVE_VALUES.contains(getStatus()) || 
          (isNotSubscribed() && new Integer(getTrialPeriod()) > 0);
    }
  }
  
  public boolean isCancelled() {
    return getStatus() != null && getStatus().equals(Status.CANCELLED.description);
  }
  
  public boolean isFree() {
    return getStatus() != null && getStatus().equals(Status.FREE.description);
  }
  
  public boolean isNotSubscribed() {
    return getStatus() != null && getStatus().equals(Status.NOT_SUBSCRIBED.description);
  }
  
  public boolean isNotSubscribed() {
    return getStatus() != null && getStatus().equals(Status.NOT_SUBSCRIBED.description);
  }
  
  public boolean isOnTrial() {
    return getStatus() != null && getStatus().equals(Status.ON_TRIAL.description);
  }
  
  public boolean isSubscribed() {
    return getStatus() != null && getStatus().equals(Status.SUBSCRIBED.description);
  }
  
  public boolean isSuspended() {
    return getStatus() != null && getStatus().equals(Status.SUSPENDED.description);
  }
  
  protected static List<String> buildList(Status... values) {
    List<String> result = new ArrayList<String>();
    
    for(Status value : values) {
      result.add(value.description);
    }
    
    return result;
  }
}
