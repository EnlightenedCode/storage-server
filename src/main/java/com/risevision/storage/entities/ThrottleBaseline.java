package com.risevision.storage.entities;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Id;

@Entity
public class ThrottleBaseline {
  @Id Long id;
  double mean;
  double sd;
  @Index Date date = new Date();

  ThrottleBaseline() {}
  public ThrottleBaseline(double mean, double sd) {
    this.mean = mean;
    this.sd = sd;
  }

  public double getMean() {
    return mean;
  }

  public double getSD() {
    return sd;
  }
}
