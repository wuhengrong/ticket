package com.grace.ticket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "vip_card")
public class VipCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;
    
    @Column(name = "card_password", nullable = false)
    private String cardPassword;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;
    
    @Column(name = "first_use_time")
    private LocalDateTime firstUseTime;
    
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
    
    @Column(name = "boarding_time")
    private LocalDateTime boardingTime;
    
    @Column(name = "boarding_station")
    private String boardingStation;
    
    @Column(name = "alighting_time")
    private LocalDateTime alightingTime;
    
    @Column(name = "alighting_station")
    private String alightingStation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "in_out_status")
    private InOutStatus inOutStatus;
    
    @Column(name = "estimated_alighting_time")
    private LocalDateTime estimatedAlightingTime;
    
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // 新增预约用户字段
    @Column(name = "reserved_user")
    private String reservedUser;
    
    // 新增备注字段
    @Column(name = "remark", length = 500)
    private String remark;
    
    // ... 现有方法 ...
    
    // 新增备注的getter和setter
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getReservedUser() {
        return reservedUser;
    }
    
    public void setReservedUser(String reservedUser) {
        this.reservedUser = reservedUser;
    }
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
    
    public enum CardStatus { 
        AVAILABLE, IN_USE, RESERVED,UNAVAILABLE, STANDBY
    }
    
    public enum InOutStatus {
        IN, OUT
    }
    
    // Constructors, Getters and Setters
    public VipCard() {}
    
    public VipCard(String cardNumber, String cardPassword, CardStatus status) {
        this.cardNumber = cardNumber;
        this.cardPassword = cardPassword;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardPassword() { return cardPassword; }
    public void setCardPassword(String cardPassword) { this.cardPassword = cardPassword; }
    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
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
    public InOutStatus getInOutStatus() { return inOutStatus; }
    public void setInOutStatus(InOutStatus inOutStatus) { this.inOutStatus = inOutStatus; }
    public LocalDateTime getEstimatedAlightingTime() { return estimatedAlightingTime; }
    public void setEstimatedAlightingTime(LocalDateTime estimatedAlightingTime) { this.estimatedAlightingTime = estimatedAlightingTime; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}