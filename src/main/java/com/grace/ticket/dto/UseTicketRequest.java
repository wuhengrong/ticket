package com.grace.ticket.dto;

public class UseTicketRequest {
    private Long vipCustomerId;
    private Long vipCardId;
    private String boardingStation;
    private String alightingStation;
    
    // Constructors
    public UseTicketRequest() {}
    
    // Getters and Setters
    public Long getVipCustomerId() { return vipCustomerId; }
    public void setVipCustomerId(Long vipCustomerId) { this.vipCustomerId = vipCustomerId; }
    public Long getVipCardId() { return vipCardId; }
    public void setVipCardId(Long vipCardId) { this.vipCardId = vipCardId; }
    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }
    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }
}