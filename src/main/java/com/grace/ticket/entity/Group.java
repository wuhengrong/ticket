package com.grace.ticket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

//Group.java
@Data
@Entity
@Table(name = "groups")
public class Group {
 public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getDefaultInterval() {
		return defaultInterval;
	}

	public void setDefaultInterval(Integer defaultInterval) {
		this.defaultInterval = defaultInterval;
	}

	public String getVirtualCardId() {
		return virtualCardId;
	}

	public void setVirtualCardId(String virtualCardId) {
		this.virtualCardId = virtualCardId;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

@Id
 @Column(name = "group_id")
 private String groupId;
 
 @Column(name = "group_name")
 private String groupName;
 
 @Column(name = "default_interval")
 private Integer defaultInterval;
 
 @Column(name = "virtual_card_id")
 private String virtualCardId;
 
 @Column(name = "create_time")
 private LocalDateTime createTime;
 
 private String status;
 
 public boolean isActive() {
     return "ACTIVE".equals(status);
 }
}