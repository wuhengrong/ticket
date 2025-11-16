package com.grace.ticket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vip_qr_records")
public class VipQrRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    // 新增字段
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "nick_name") 
    private String nickName;
    
    @Column(name = "start_station")
    private String startStation;
    
    @Column(name = "end_station")
    private String endStation;
    
    @Column(name = "qr_url")
    private String qrUrl;
    
    private String status; // ACTIVE, USED, EXPIRED
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 构造函数
    public VipQrRecord() {}
    
    // getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    // 新增getter和setter
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getStartStation() {
        return startStation;
    }
    
    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }
    
    public String getEndStation() {
        return endStation;
    }
    
    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }
    
    public String getQrUrl() {
        return qrUrl;
    }
    
    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}