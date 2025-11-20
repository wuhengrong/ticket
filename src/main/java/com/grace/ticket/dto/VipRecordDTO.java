// VipRecordDTO.java
package com.grace.ticket.dto;

import java.time.LocalDateTime;

public class VipRecordDTO {
    private Long id;
    private Long vipCustomerId;
    private String customerName;
    private String customerNickName;
    private Long vipCardId;
    private String cardNumber;
    private String boardingStation;
    private String alightingStation;
    private LocalDateTime boardingTime;
    private LocalDateTime alightingTime;
    private LocalDateTime estimatedAlightingTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	private String status;
    
    // Constructors
    public VipRecordDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVipCustomerId() { return vipCustomerId; }
    public void setVipCustomerId(Long vipCustomerId) { this.vipCustomerId = vipCustomerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerNickName() { return customerNickName; }
    public void setCustomerNickName(String customerNickName) { this.customerNickName = customerNickName; }
    public Long getVipCardId() { return vipCardId; }
    public void setVipCardId(Long vipCardId) { this.vipCardId = vipCardId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }
    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }
    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }
    public LocalDateTime getAlightingTime() { return alightingTime; }
    public void setAlightingTime(LocalDateTime alightingTime) { this.alightingTime = alightingTime; }
    public LocalDateTime getEstimatedAlightingTime() { return estimatedAlightingTime; }
    public void setEstimatedAlightingTime(LocalDateTime estimatedAlightingTime) { this.estimatedAlightingTime = estimatedAlightingTime; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}