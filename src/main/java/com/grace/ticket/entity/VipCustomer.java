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
@Table(name = "vip_customer")
public class VipCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "group_id")
    private Long groupId;
    
    @Column(name = "group_name")
    private String groupName;
    
    @Column(name = "ride_count", nullable = false)
    private Integer rideCount = 0;
    
    @Column(name = "vip_url", unique = true)
    private String vipUrl;
    
    private String remark;
    
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // 新增昵称字段
    @Column(name = "nick_name")
    private String nickName;
    
 // 修改为简单的字段配置
    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType = "VIP"; // 只在Java层面设置默认值
    
    // 客户类别枚举值
    public static class CustomerType {
        public static final String GENERAL = "PRIMARY";      // 只能使用链接次卡
        public static final String LIMITED = "LIMITED";      // 特定时间用过的次卡 + 链接次卡
        public static final String NORMAL = "NORMAL";       // 只能用已经用过的次卡
        public static final String PLATINUM = "VIP";     // 用过的次卡 + 新的次卡
        public static final String BLACK = "SVIP";        // 白金 + 备用卡
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
    
    // Constructors, Getters and Setters
    public VipCustomer() {}
    
    public VipCustomer(String userName, String vipUrl, Integer rideCount) {
        this.userName = userName;
        this.vipUrl = vipUrl;
        this.rideCount = rideCount;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Integer getRideCount() { return rideCount; }
    public void setRideCount(Integer rideCount) { this.rideCount = rideCount; }
    public String getVipUrl() { return vipUrl; }
    public void setVipUrl(String vipUrl) { this.vipUrl = vipUrl; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
}