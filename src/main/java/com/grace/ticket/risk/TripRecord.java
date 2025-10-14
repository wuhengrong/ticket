package com.grace.ticket.risk;

import java.time.LocalDateTime;

public class TripRecord {
    private String startStation;
    private LocalDateTime startTime;
    private String endStation;
    private LocalDateTime endTime;
    
    public TripRecord(String startStation, LocalDateTime startTime, 
                     String endStation, LocalDateTime endTime) {
        this.startStation = startStation;
        this.startTime = startTime;
        this.endStation = endStation;
        this.endTime = endTime;
    }
    
    // getters
    public String getStartStation() { return startStation; }
    public LocalDateTime getStartTime() { return startTime; }
    public String getEndStation() { return endStation; }
    public LocalDateTime getEndTime() { return endTime; }
}