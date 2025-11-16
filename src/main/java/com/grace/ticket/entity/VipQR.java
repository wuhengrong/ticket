package com.grace.ticket.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vip_qr")
public class VipQR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_url", nullable = false, unique = true)
    private String cardUrl;

    @Column(name = "ride_count", nullable = false)
    private Integer rideCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QRStatus status = QRStatus.AVAILABLE;

    @Column(name = "creator")
    private String creator;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "used_time")
    private LocalDateTime usedTime;

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    @Column(name = "remark")
    private String remark;

    // 枚举定义
    public enum QRStatus {
        AVAILABLE,      // 可用
        IN_USE,         // 使用中
        USED,           // 已使用
        EXPIRED,        // 已过期
        DISABLED        // 禁用
    }

    // Constructors
    public VipQR() {}

    public VipQR(String cardUrl, Integer rideCount, String creator) {
        this.cardUrl = cardUrl;
        this.rideCount = rideCount;
        this.creator = creator;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardUrl() { return cardUrl; }
    public void setCardUrl(String cardUrl) { this.cardUrl = cardUrl; }

    public Integer getRideCount() { return rideCount; }
    public void setRideCount(Integer rideCount) { this.rideCount = rideCount; }

    public QRStatus getStatus() { return status; }
    public void setStatus(QRStatus status) { this.status = status; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getUsedTime() { return usedTime; }
    public void setUsedTime(LocalDateTime usedTime) { this.usedTime = usedTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}