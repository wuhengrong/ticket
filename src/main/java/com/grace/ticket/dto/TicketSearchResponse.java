package com.grace.ticket.dto;

import java.time.LocalDateTime;

public class TicketSearchResponse {
    private boolean success;
    private String message;
    private VipCardDTO matchedCard;
    private LocalDateTime estimatedAlightingTime;
    private Integer remainingRides;
    
    // Constructors
    public TicketSearchResponse() {}
    
    public static TicketSearchResponse success(VipCardDTO matchedCard, LocalDateTime estimatedAlightingTime, Integer remainingRides) {
        TicketSearchResponse response = new TicketSearchResponse();
        response.success = true;
        response.message = "找到匹配票卡";
        response.matchedCard = matchedCard;
        response.estimatedAlightingTime = estimatedAlightingTime;
        response.remainingRides = remainingRides;
        return response;
    }
    
    public static TicketSearchResponse failure(String message) {
        TicketSearchResponse response = new TicketSearchResponse();
        response.success = false;
        response.message = message;
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public VipCardDTO getMatchedCard() { return matchedCard; }
    public void setMatchedCard(VipCardDTO matchedCard) { this.matchedCard = matchedCard; }
    public LocalDateTime getEstimatedAlightingTime() { return estimatedAlightingTime; }
    public void setEstimatedAlightingTime(LocalDateTime estimatedAlightingTime) { this.estimatedAlightingTime = estimatedAlightingTime; }
    public Integer getRemainingRides() { return remainingRides; }
    public void setRemainingRides(Integer remainingRides) { this.remainingRides = remainingRides; }
}