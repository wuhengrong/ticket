package com.grace.ticket.risk;

//交通路线结果类
public class RouteResult {
 private String mode;
 private int duration; // 分钟
 private int distance; // 米
 private double cost; // 元
 
 public RouteResult(String mode, int duration, int distance, double cost) {
     this.mode = mode;
     this.duration = duration;
     this.distance = distance;
     this.cost = cost;
 }
 
 // getters
 public String getMode() { return mode; }
 public int getDuration() { return duration; }
 public int getDistance() { return distance; }
 public double getCost() { return cost; }
}