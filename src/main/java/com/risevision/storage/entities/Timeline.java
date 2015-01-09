package com.risevision.storage.entities;

public class Timeline {
  private boolean timeDefined;
  private String startDate;
  private String endDate;
  private String startTime;
  private String endTime;
  private boolean pud;
  private boolean trash;
  private boolean carryon;
  private Integer duration;

  public boolean isTimeDefined() {
    return timeDefined;
  }

  public void setTimeDefined(boolean timeDefined) {
    this.timeDefined = timeDefined;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public boolean isPud() {
    return pud;
  }

  public void setPud(boolean pud) {
    this.pud = pud;
  }

  public boolean isTrash() {
    return trash;
  }

  public void setTrash(boolean trash) {
    this.trash = trash;
  }

  public boolean isCarryon() {
    return carryon;
  }

  public void setCarryon(boolean carryon) {
    this.carryon = carryon;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }
}
