package com.risevision.storage.entities.core;

import java.util.List;

public class CoreUser {
  private String id;
  private String companyId;
  private String username;
  private String creationDate;
  private String email;
  private String lastLogin;
  private Integer status;
  private List<String> roles;
  private String termsAcceptanceDate;
  private String showTutorial;
  private boolean mailSyncEnabled;
  private String changedBy;
  private String changeDate;
  
  public CoreUser() {
    
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(String lastLogin) {
    this.lastLogin = lastLogin;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public String getTermsAcceptanceDate() {
    return termsAcceptanceDate;
  }

  public void setTermsAcceptanceDate(String termsAcceptanceDate) {
    this.termsAcceptanceDate = termsAcceptanceDate;
  }

  public String getShowTutorial() {
    return showTutorial;
  }

  public void setShowTutorial(String showTutorial) {
    this.showTutorial = showTutorial;
  }

  public boolean isMailSyncEnabled() {
    return mailSyncEnabled;
  }

  public void setMailSyncEnabled(boolean mailSyncEnabled) {
    this.mailSyncEnabled = mailSyncEnabled;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }
}
