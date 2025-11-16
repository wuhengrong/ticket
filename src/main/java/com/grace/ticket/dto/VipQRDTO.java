package com.grace.ticket.dto;



import java.time.LocalDateTime;

import com.grace.ticket.entity.VipQR;

public class VipQRDTO {
    private Long id;
    private String cardUrl;
    private Integer rideCount;
    private VipQR.QRStatus status;
    private String creator;
    private String userName;
    private LocalDateTime usedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String remark;

    // Constructors
    public VipQRDTO() {}

    public VipQRDTO(VipQR qr) {
        this.id = qr.getId();
        this.cardUrl = qr.getCardUrl();
        this.rideCount = qr.getRideCount();
        this.status = qr.getStatus();
        this.creator = qr.getCreator();
        this.userName = qr.getUserName();
        this.usedTime = qr.getUsedTime();
        this.createdTime = qr.getCreatedTime();
        this.updatedTime = qr.getUpdatedTime();
        this.remark = qr.getRemark();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardUrl() { return cardUrl; }
    public void setCardUrl(String cardUrl) { this.cardUrl = cardUrl; }

    public Integer getRideCount() { return rideCount; }
    public void setRideCount(Integer rideCount) { this.rideCount = rideCount; }

    public VipQR.QRStatus getStatus() { return status; }
    public void setStatus(VipQR.QRStatus status) { this.status = status; }

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