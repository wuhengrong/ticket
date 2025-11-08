package com.grace.ticket.dto;

import java.time.LocalDateTime;

public class TicketSearchRequest {
    private String boardingStation;
    private String alightingStation;
    private LocalDateTime boardingTime;
    private Long vipCustomerId;
    
    // Constructors
    public TicketSearchRequest() {}
    
    public TicketSearchRequest(String boardingStation, String alightingStation, LocalDateTime boardingTime) {
        this.boardingStation = boardingStation;
        this.alightingStation = alightingStation;
        this.boardingTime = boardingTime;
    }
    
    // Getters and Setters
    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }
    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }
    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }
    public Long getVipCustomerId() { return vipCustomerId; }
    public void setVipCustomerId(Long vipCustomerId) { this.vipCustomerId = vipCustomerId; }
}