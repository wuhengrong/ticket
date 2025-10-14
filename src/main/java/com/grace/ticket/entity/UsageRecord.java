package com.grace.ticket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

//UsageRecord.java
@Data
@Entity
@Table(name = "usage_records")
public class UsageRecord {
 @Id
 @Column(name = "record_id")
 private String recordId;
 
 @Column(name = "group_id")
 private String groupId;
 
 public String getRecordId() {
	return recordId;
}

public void setRecordId(String recordId) {
	this.recordId = recordId;
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

public String getVirtualCardId() {
	return virtualCardId;
}

public void setVirtualCardId(String virtualCardId) {
	this.virtualCardId = virtualCardId;
}

public LocalDateTime getStartTime() {
	return startTime;
}

public void setStartTime(LocalDateTime startTime) {
	this.startTime = startTime;
}

public LocalDateTime getEndTime() {
	return endTime;
}

public void setEndTime(LocalDateTime endTime) {
	this.endTime = endTime;
}

public Integer getDuration() {
	return duration;
}

public void setDuration(Integer duration) {
	this.duration = duration;
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

@Column(name = "user_id")
 private String userId;
 
 @Column(name = "virtual_card_id")
 private String virtualCardId;
 
 @Column(name = "start_time")
 private LocalDateTime startTime;
 
 @Column(name = "end_time")
 private LocalDateTime endTime;
 
 private Integer duration;
 
 private String status;
 
 @Column(name = "create_time")
 private LocalDateTime createTime;
 
 public boolean isInProgress() {
     return "IN_PROGRESS".equals(status);
 }
 
 public boolean isCompleted() {
     return "COMPLETED".equals(status);
 }
}