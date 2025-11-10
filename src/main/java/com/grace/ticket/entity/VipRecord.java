package com.grace.ticket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "vip_record")
public class VipRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vip_customer_id", nullable = false)
    private Long vipCustomerId;
    
    @Column(name = "vip_card_id", nullable = false)
    private Long vipCardId;
    
    @Column(name = "boarding_station", nullable = false)
    private String boardingStation;
    
    @Column(name = "alighting_station")
    private String alightingStation;
    
    @Column(name = "boarding_time", nullable = false)
    private LocalDateTime boardingTime;
    
    @Column(name = "alighting_time")
    private LocalDateTime alightingTime;
    
    @Column(name = "estimated_alighting_time")
    private LocalDateTime estimatedAlightingTime;
    
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
    
    // Constructors, Getters and Setters
    public VipRecord() {}
    
    public VipRecord(Long vipCustomerId, Long vipCardId, String boardingStation, LocalDateTime boardingTime, String alightingStation) {
        this.vipCustomerId = vipCustomerId;
        this.vipCardId = vipCardId;
        this.boardingStation = boardingStation;
        this.boardingTime = boardingTime;
        this.alightingStation = alightingStation;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVipCustomerId() { return vipCustomerId; }
    public void setVipCustomerId(Long vipCustomerId) { this.vipCustomerId = vipCustomerId; }
    public Long getVipCardId() { return vipCardId; }
    public void setVipCardId(Long vipCardId) { this.vipCardId = vipCardId; }
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