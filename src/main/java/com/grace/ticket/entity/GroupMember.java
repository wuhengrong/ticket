package com.grace.ticket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

//GroupMember.java
@Data
@Entity
@Table(name = "group_members")
public class GroupMember {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(name = "group_id")
 private String groupId;
 
 @Column(name = "user_id")
 private String userId;
 
 @Column(name = "use_order")
 private Integer useOrder;
 
 @Column(name = "access_code", unique = true, length = 32)
 private String accessCode;
 
 @Column(name = "user_name", length = 100)
 private String userName;
 
 // getter 和 setter
 public String getUserName() {
     return userName;
 }
 
 public void setUserName(String userName) {
     this.userName = userName;
 }
 
 // getter 和 setter
 public String getAccessCode() {
     return accessCode;
 }
 
 public void setAccessCode(String accessCode) {
     this.accessCode = accessCode;
 }
 public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public String getGroupId() {
	return groupId;
}

public void setGroupId(String groupId) {
	this.groupId = groupId;
}

public String getUserId() {
	return userId;
}

public void setUserId(String userId) {
	this.userId = userId;
}

public Integer getUseOrder() {
	return useOrder;
}

public void setUseOrder(Integer useOrder) {
	this.useOrder = useOrder;
}

public Integer getCustomInterval() {
	return customInterval;
}

public void setCustomInterval(Integer customInterval) {
	this.customInterval = customInterval;
}

public String getUserCardStartTime() {
	return userCardStartTime;
}

public void setUserCardStartTime(String userCardStartTime) {
	this.userCardStartTime = userCardStartTime;
}

public String getUserCardEndTime() {
	return userCardEndTime;
}

public void setUserCardEndTime(String userCardEndTime) {
	this.userCardEndTime = userCardEndTime;
}

public String getStatus() {
	return status;
}

public void setStatus(String status) {
	this.status = status;
}

public String getPersonalUrl() {
	return personalUrl;
}

public void setPersonalUrl(String personalUrl) {
	this.personalUrl = personalUrl;
}
@Column(name = "password_special_time")
private String passwordSpecialTime; // 格式: "07:00"

// getter 和 setter
public String getPasswordSpecialTime() {
    return passwordSpecialTime;
}

public void setPasswordSpecialTime(String passwordSpecialTime) {
    this.passwordSpecialTime = passwordSpecialTime;
}

@Column(name = "custom_interval")
 private Integer customInterval;
 
 @Column(name = "user_card_start_time")
 private String userCardStartTime;
 
 @Column(name = "user_card_end_time")
 private String userCardEndTime;
 
 private String status;
 
 @Column(name = "personal_url")
 private String personalUrl;
 
 public Integer getEffectiveInterval() {
     return customInterval != null ? customInterval : 0;
 }
 
 public boolean isActive() {
     return "ACTIVE".equals(status);
 }
}