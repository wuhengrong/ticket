package com.grace.ticket.dto;


import java.time.LocalDateTime;

import com.grace.ticket.entity.VipCard;

public class VipCardDTO {
    private Long id;
    private String cardNumber;
    private String cardPassword;
    private VipCard.CardStatus status;
    private LocalDateTime firstUseTime;
    private LocalDateTime expiryTime;
    private LocalDateTime boardingTime;
    private String boardingStation;
    private LocalDateTime alightingTime;
    private String alightingStation;
    private VipCard.InOutStatus inOutStatus;
    private LocalDateTime estimatedAlightingTime;
    
    private String remark;
    
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    // Constructors
    public VipCardDTO() {}
    
    public VipCardDTO(VipCard vipCard) {
        this.id = vipCard.getId();
        this.cardNumber = vipCard.getCardNumber();
        this.cardPassword = vipCard.getCardPassword();
        this.status = vipCard.getStatus();
        this.firstUseTime = vipCard.getFirstUseTime();
        this.expiryTime = vipCard.getExpiryTime();
        this.boardingTime = vipCard.getBoardingTime();
        this.boardingStation = vipCard.getBoardingStation();
        this.alightingTime = vipCard.getAlightingTime();
        this.alightingStation = vipCard.getAlightingStation();
        this.inOutStatus = vipCard.getInOutStatus();
        this.estimatedAlightingTime = vipCard.getEstimatedAlightingTime();
        this.reservedUser = vipCard.getReservedUser();
        this.remark = vipCard.getRemark();
    }
    
    // 新增预约用户字段
    private String reservedUser;
    
    // 构造方法、getter、setter...
    
    public String getReservedUser() {
        return reservedUser;
    }
    
    public void setReservedUser(String reservedUser) {
        this.reservedUser = reservedUser;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardPassword() { return cardPassword; }
    public void setCardPassword(String cardPassword) { this.cardPassword = cardPassword; }
    public VipCard.CardStatus getStatus() { return status; }
    public void setStatus(VipCard.CardStatus status) { this.status = status; }
    public LocalDateTime getFirstUseTime() { return firstUseTime; }
    public void setFirstUseTime(LocalDateTime firstUseTime) { this.firstUseTime = firstUseTime; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }
    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }
    public LocalDateTime getAlightingTime() { return alightingTime; }
    public void setAlightingTime(LocalDateTime alightingTime) { this.alightingTime = alightingTime; }
    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }
    public VipCard.InOutStatus getInOutStatus() { return inOutStatus; }
    public void setInOutStatus(VipCard.InOutStatus inOutStatus) { this.inOutStatus = inOutStatus; }
    public LocalDateTime getEstimatedAlightingTime() { return estimatedAlightingTime; }
    public void setEstimatedAlightingTime(LocalDateTime estimatedAlightingTime) { this.estimatedAlightingTime = estimatedAlightingTime; }
}