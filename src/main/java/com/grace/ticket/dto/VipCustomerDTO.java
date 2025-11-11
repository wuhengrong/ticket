package com.grace.ticket.dto;

import com.grace.ticket.entity.VipCustomer;

import java.time.LocalDateTime;

public class VipCustomerDTO {
    private Long id;
    private String userName;
    private Long groupId;
    private String groupName;
    private Integer rideCount;
    private String vipUrl;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    
    // 新增昵称字段
    private String nickName;
    
    // 构造方法、getter、setter...
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    // Constructors
    public VipCustomerDTO() {}
    
    public VipCustomerDTO(VipCustomer customer) {
        this.id = customer.getId();
        this.userName = customer.getUserName();
        this.groupId = customer.getGroupId();
        this.groupName = customer.getGroupName();
        this.rideCount = customer.getRideCount();
        this.vipUrl = customer.getVipUrl();
        this.remark = customer.getRemark();
        this.createdTime = customer.getCreatedTime();
        this.updatedTime = customer.getUpdatedTime();
        this.nickName = customer.getNickName();
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
}