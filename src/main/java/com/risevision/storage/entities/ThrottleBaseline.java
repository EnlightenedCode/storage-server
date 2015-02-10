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
  double min;
  double max;
  @Index Date date = new Date();

  ThrottleBaseline() {}
  public ThrottleBaseline(double mean, double sd, double min, double max) {
    this.mean = mean;
    this.sd = sd;
    this.min = min;
    this.max = max;
  }

  public double getMean() {
    return mean;
  }

  public double getSD() {
    return sd;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }
}
