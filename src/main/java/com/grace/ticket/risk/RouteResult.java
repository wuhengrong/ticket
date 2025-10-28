package com.grace.ticket.risk;

//交通路线结果类
public class RouteResult {
 private String mode;
 private int duration; // 分钟
 private int pureSubwayTime; // 分钟
 
 private int distance; // 米
 private double cost; // 元
 
 public RouteResult(String mode, int duration, int pureSubwayTime, int distance, double cost) {
     this.mode = mode;
     this.duration = duration;
     this.pureSubwayTime = pureSubwayTime;
     this.distance = distance;
     this.cost = cost;
 }
 
 // getters
 public String getMode() { return mode; }
 public int getPureSubwayTime() {
	return pureSubwayTime;
}

public void setPureSubwayTime(int pureSubwayTime) {
	this.pureSubwayTime = pureSubwayTime;
}

public void setMode(String mode) {
	this.mode = mode;
}

public void setDuration(int duration) {
	this.duration = duration;
}

public void setDistance(int distance) {
	this.distance = distance;
}

public void setCost(double cost) {
	this.cost = cost;
}

public int getDuration() { return duration; }
 public int getDistance() { return distance; }
 public double getCost() { return cost; }
}